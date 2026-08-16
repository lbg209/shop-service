package com.lbg0146.shop_service.batch.job;

import com.lbg0146.shop_service.batch.dto.MemberPurchaseSumDto;
import com.lbg0146.shop_service.grade.entity.Grade;
import com.lbg0146.shop_service.grade.repository.GradeRepository;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.repository.MemberRepository;
import com.lbg0146.shop_service.member.service.MemberHistoryService;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GradeUpdateBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final MemberRepository memberRepository;
    private final GradeRepository gradeRepository;
    private final MemberHistoryService memberHistoryService;

    private static final int CHUNK_SIZE = 1000;


    @Bean
    public Job gradeUpdateJob() {
        return new JobBuilder("gradeUpdateJob", jobRepository)
                .start(gradeUpdateStep())
                .build();
    }

    @Bean
    public Step gradeUpdateStep() {
        return new StepBuilder("gradeUpdateStep", jobRepository)

                .<MemberPurchaseSumDto, Member>chunk(CHUNK_SIZE)
                .reader(gradeUpdateReader(null))
                .processor(gradeUpdateProcessor(memberRepository, gradeRepository))
                .writer(gradeUpdateWriter(memberRepository, memberHistoryService))
                .transactionManager(transactionManager)
                .build();
    }


    // ========== [1. Reader: 파라미터로 받은 '기준 월'의 주문 데이터 읽기] ==========
    @Bean
    @StepScope // 파라미터를 동적으로 받으려면 반드시 필요!
    public JpaPagingItemReader<MemberPurchaseSumDto> gradeUpdateReader(
            // ★ 컨트롤러에서 넘겨준 "2026-08" 값을 여기서 받음!
            @Value("#{jobParameters['targetMonth']}") String targetMonthStr
    ) {
        // 1. 파라미터가 혹시 안 들어오면 기본값으로 '지난달'을 쓰도록 방어 로직 추가
        YearMonth targetMonth;

        if (targetMonthStr == null || targetMonthStr.isEmpty()) {
            targetMonth = YearMonth.now().minusMonths(1);
        } else {
            targetMonth = YearMonth.parse(targetMonthStr); // "2026-08" -> YearMonth 객체로 변환
        }

        // 2. 받은 월을 기준으로 시작일과 종료일 계산 (예: 8월 1일 ~ 9월 1일)
        LocalDateTime startDate = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = targetMonth.plusMonths(1).atDay(1).atStartOfDay();

        log.info("📅 배치 기준 월: {}, 조회 범위: {} ~ {}", targetMonth, startDate, endDate);

        return new JpaPagingItemReaderBuilder<MemberPurchaseSumDto>()
                .name("gradeUpdateReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString(
                        "SELECT new com.lbg0146.shop_service.batch.dto.MemberPurchaseSumDto(" +
                                "m.id, COALESCE(SUM(o.totalPrice), 0L)) " +
                                "FROM Member m " +
                                "LEFT JOIN Order o ON o.member.id = m.id " +
                                "AND o.orderedAt >= :startDate " +
                                "AND o.orderedAt < :endDate " +
                                "AND o.orderStatus.codeValue IN ('PAID', 'SHIPPING', 'DELIVERED') " +
                                "AND o.orderStatus.group.groupCode = 'ORDER_STATUS' " +
                                "GROUP BY m.id"
                )
                .parameterValues(Map.of(
                        "startDate", startDate,
                        "endDate", endDate
                ))
                .build();
    }

    // ========== [2. Processor: 읽어온 금액 기반으로 등급 변경 판단 로직] ==========
    @Bean
    public ItemProcessor<MemberPurchaseSumDto, Member> gradeUpdateProcessor(MemberRepository memberRepository, GradeRepository gradeRepository) {
        return dto -> {
            log.info("🔍 검사 중 -> 회원 ID: {}, 누적 구매금액: {}", dto.memberId(), dto.totalPurchaseAmount());

            // 1. 회원 엔티티 조회 (없으면 필터링)
            Member member = memberRepository.findById(dto.memberId()).orElse(null);
            if (member == null) {
                return null;
            }

            // 2. 높은 등급 기준부터 내림차순으로 비교 (예: VIP(50만) -> GOLD(10만) -> BASIC(0))
            List<Grade> grades = gradeRepository.findAllByOrderByMinPurchaseAmountDesc();

            Grade targetGrade = grades.stream()
                    .filter(g -> dto.totalPurchaseAmount() >= g.getMinPurchaseAmount().longValue())
                    .findFirst()
                    .orElse(null);

            // 3. 적합한 등급이 없거나, 이미 해당 등급이면 필터링 (Writer로 안 넘김)
            if (targetGrade == null || member.getGrade().getId().equals(targetGrade.getId())) {
                return null;
            }

            // 4. 등급이 다르면 승급(또는 강등) 대상! 엔티티 업데이트 후 반환
            log.info("🎉 대상자 발견! 회원 ID: {} - 등급 변경 ({} -> {})",
                    member.getId(), member.getGrade().getGradeCode(), targetGrade.getGradeCode());

            member.changeGrade(targetGrade);

            return member;
        };
    }

    // ========== [3. Writer: 변경된 회원 엔티티 DB에 저장] ==========
    @Bean
    public ItemWriter<Member> gradeUpdateWriter(MemberRepository memberRepository, MemberHistoryService memberHistoryService) {
        return chunk -> {
            log.info("💾 DB 업데이트 및 이력 저장 시작... (총 {}명)", chunk.size());

            LocalDateTime now = LocalDateTime.now();

            for (Member member : chunk.getItems()) {
                // 1. 기존 히스토리 닫기 (MemberService의 로직과 동일하게 처리)
                memberHistoryService.closeCurrentHistory(member.getId(), now);

                // 2. 회원 데이터 DB에 완전 반영
                memberRepository.save(member);

                // 3. 새로운 상태의 히스토리 기록 ("UPDATE" 코드로 저장)
                memberHistoryService.saveHistory(member, "UPDATE", null);
            }

            log.info("✅ 청크 단위 처리 완료!");
        };
    }
}
