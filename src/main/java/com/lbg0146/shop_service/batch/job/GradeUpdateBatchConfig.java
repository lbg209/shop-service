package com.lbg0146.shop_service.batch.job;

import com.lbg0146.shop_service.batch.dto.MemberPurchaseSumDto;
import com.lbg0146.shop_service.batch.support.BatchExecutionTimeListener;
import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.code.repository.CommonCodeDetailRepository;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.grade.entity.Grade;
import com.lbg0146.shop_service.grade.repository.GradeRepository;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.repository.MemberHistoryBulkRepository;
import com.lbg0146.shop_service.member.repository.MemberHistoryRepository;
import com.lbg0146.shop_service.member.repository.MemberRepository;
import com.lbg0146.shop_service.member.service.MemberHistoryService;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
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
import java.util.stream.Collectors;

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
    private final BatchExecutionTimeListener batchExecutionTimeListener;
    private final CommonCodeDetailRepository commonCodeDetailRepository;
    private final MemberHistoryRepository memberHistoryRepository;
    private final MemberHistoryBulkRepository memberHistoryBulkRepository;

    private static final int CHUNK_SIZE = 1000;

    @Bean
    public Job gradeUpdateJob() {
        return new JobBuilder("gradeUpdateJob", jobRepository)
                .listener(batchExecutionTimeListener)
                .start(gradeUpdateStep())
                .build();
    }

    @Bean
    public Step gradeUpdateStep() {
        return new StepBuilder("gradeUpdateStep", jobRepository)
                .<MemberPurchaseSumDto, Member>chunk(CHUNK_SIZE)
                .reader(gradeUpdateReader(null))
                .processor(gradeUpdateProcessor(memberRepository, gradeRepository))
                .writer(gradeUpdateWriter(memberRepository, memberHistoryService, commonCodeDetailRepository))
                .transactionManager(transactionManager)
                .build();
    }

    // ========== [1. Reader: 파라미터로 받은 '기준 월'의 주문 데이터 읽기] ==========
    @Bean
    @StepScope // 파라미터를 동적으로 받으려면 필요
    public JpaPagingItemReader<MemberPurchaseSumDto> gradeUpdateReader(
            //컨트롤러에서 넘겨준 "2026-08" 값을 여기서 받음
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

        log.info("========== READER 생성 ==========");

        log.info("배치 기준 월 = {}", targetMonth);

        log.info("조회 범위 = {} ~ {}", startDate, endDate);


        return new JpaPagingItemReaderBuilder<MemberPurchaseSumDto>()
                .name("gradeUpdateReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString(
                        "SELECT new com.lbg0146.shop_service.batch.dto.MemberPurchaseSumDto(" +
                                "m, COALESCE(SUM(o.totalPrice), 0L)) " +
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
    @StepScope
    public ItemProcessor<MemberPurchaseSumDto, Member> gradeUpdateProcessor(MemberRepository memberRepository, GradeRepository gradeRepository) {

        // 높은 등급 기준부터 내림차순으로 비교 (VIP(50만) -> GOLD(10만) -> BASIC(0))
        List<Grade> grades = gradeRepository.findAllByOrderByMinPurchaseAmountDesc();

        return dto -> {

            Member member = dto.member();

            Grade targetGrade = grades.stream()
                    .filter(g -> dto.totalPurchaseAmount() >= g.getMinPurchaseAmount().longValue())
                    .findFirst()
                    .orElse(null);

            // 목표 등급 자체를 찾지 못한 경우
            if (targetGrade == null) {

                return null;
            }

            // 현재 등급과 목표 등급이 동일한 경우
            if (member.getGrade().getId().equals(targetGrade.getId())) {

                return null;
            }

            // JPA 엔티티 상태 변경
            member.changeGrade(targetGrade);

            return member;
        };
    }

    // ========== [3. Writer: 변경된 회원 엔티티 DB에 저장] ==========
    @Bean
    public ItemWriter<Member> gradeUpdateWriter(MemberRepository memberRepository, MemberHistoryService memberHistoryService, CommonCodeDetailRepository commonCodeDetailRepository) {
        return chunk -> {

            if (chunk.isEmpty()) {

                log.info("[WRITER] 처리할 Member가 없습니다.");
                log.info("========== WRITER END ==========");

                return;
            }

            CommonCodeDetail updateCode = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue("HISTORY_CHANGE_TYPE", "UPDATE")
                    .orElseThrow(() -> new BusinessException(ErrorCode.HISTORY_CHANGE_TYPE_NOT_FOUND));

            LocalDateTime now = LocalDateTime.now();

            long start = System.currentTimeMillis();

            List<Long> memberIds = chunk.getItems().stream()
                    .map(Member::getId)
                    .toList();

            //memberHistoryRepository.closeCurrentHistories(memberIds, now);
/*
            for (Member member : chunk.getItems()) {
                // 기존 히스토리 닫기
                //memberHistoryRepository.closeCurrentHistory(member.getId(), now);

                // 변경 등급 저장
                memberRepository.save(member);

                //. 새로운 상태의 히스토리 기록
                memberHistoryService.saveHistory(member,  updateCode, null);
            }
*/
            long start1 = System.currentTimeMillis();

            long historyCloseStart = System.currentTimeMillis();

            memberHistoryRepository.closeCurrentHistories(memberIds, now);

            long historyCloseEnd = System.currentTimeMillis();

            long memberSaveStart = System.currentTimeMillis();

            Map<Grade, List<Long>> memberIdsByGrade = chunk.getItems().stream()
                    .collect(Collectors.groupingBy(
                            Member::getGrade,
                            Collectors.mapping(Member::getId, Collectors.toList())
                    ));

            for (Map.Entry<Grade, List<Long>> entry : memberIdsByGrade.entrySet()) {
                memberRepository.updateGradeByIds(
                        entry.getValue(),
                        entry.getKey()
                );
            }
            //for (Member member : chunk.getItems()) {
            //    memberRepository.save(member);
            //}

            long memberSaveEnd = System.currentTimeMillis();

            long historySaveStart = System.currentTimeMillis();

            memberHistoryBulkRepository.saveAll(
                    chunk.getItems(),
                    updateCode,
                    null
            );

            long historySaveEnd = System.currentTimeMillis();

            log.info(
                    "[GRADE UPDATE] closeHistory={}ms, memberSave={}ms, historySave={}ms, total={}ms, count={}",
                    historyCloseEnd - historyCloseStart,
                    memberSaveEnd - memberSaveStart,
                    historySaveEnd - historySaveStart,
                    historySaveEnd - start1,
                    chunk.size()
            );

            long end = System.currentTimeMillis();

            log.info(
                    "[GRADE UPDATE] Writer 처리시간={}ms, 처리건수={}",
                    end - start,
                    chunk.size()
            );
        };
    }
}
