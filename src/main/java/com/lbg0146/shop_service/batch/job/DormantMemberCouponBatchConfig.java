package com.lbg0146.shop_service.batch.job;

import com.lbg0146.shop_service.batch.support.BatchExecutionTimeListener;
import com.lbg0146.shop_service.coupon.entity.Coupon;
import com.lbg0146.shop_service.coupon.entity.MemberCoupon;
import com.lbg0146.shop_service.coupon.repository.CouponRepository;
import com.lbg0146.shop_service.coupon.repository.MemberCouponRepository;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.member.entity.Member;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
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
import java.util.Map;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DormantMemberCouponBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final MemberCouponRepository memberCouponRepository;
    private final CouponRepository couponRepository;
    private final BatchExecutionTimeListener batchExecutionTimeListener;

    private static final int CHUNK_SIZE = 1000;

    @Bean
    public Job dormantMemberCouponJob() {
        return new JobBuilder("dormantMemberCouponJob", jobRepository)
                .listener(batchExecutionTimeListener)
                .start(dormantMemberCouponStep())
                .build();
    }

    @Bean
    public Step dormantMemberCouponStep() {
        return new StepBuilder("dormantMemberCouponStep", jobRepository)

                .<Member, MemberCoupon>chunk(CHUNK_SIZE)
                .reader(dormantMemberReader())
                .processor(dormantMemberProcessor(null))
                .writer(dormantMemberWriter())
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Member> dormantMemberReader() {
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

        log.info("미주문 고객 조회 기준일: {}", threeMonthsAgo);

        return new JpaPagingItemReaderBuilder<Member>()
                .name("dormantMemberReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString(
                        "SELECT m FROM Member m " +
                                "WHERE m.createdAt < :threeMonthsAgo " +
                                "AND m.deletedAt IS NULL " +
                                "AND NOT EXISTS (" +
                                "    SELECT 1 FROM Order o " +
                                "    WHERE o.member = m " +
                                "    AND o.orderedAt >= :threeMonthsAgo " +
                                "    AND o.orderStatus.codeValue IN ('PAID', 'SHIPPING', 'DELIVERED')" +
                                ")"
                )
                .parameterValues(Map.of("threeMonthsAgo", threeMonthsAgo))
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Member, MemberCoupon> dormantMemberProcessor(@Value("${app.batch.dormant-coupon-id}") Long targetCouponId) {

        // 배치가 시작될 때 쿠폰을 한 번만 조회해 둡니다.
        Coupon targetCoupon = couponRepository.findByIdAndDeletedAtIsNull(targetCouponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        long start = System.currentTimeMillis();
        // 이미 해당 쿠폰을 받은 회원 ID를 한 번에 조회
        Set<Long> alreadyIssuedMemberIds =
                memberCouponRepository.findMemberIdsByCouponId(targetCouponId);

        long end = System.currentTimeMillis();

        log.info("기존 쿠폰 발급 회원 조회 시간: {} ms", end - start);

        log.info("이미 쿠폰을 발급받은 회원 수: {}", alreadyIssuedMemberIds.size());
        log.info("쿠폰 로드 완료: {}", targetCoupon.getCouponName());

        return member -> {
/*
            // 이미 받았는지 중복 검사
            boolean alreadyIssued = memberCouponRepository.existsByMemberIdAndCouponId(member.getId(), targetCouponId);

            if (alreadyIssued) {
                return null; // 스킵
            }
*/
            if (alreadyIssuedMemberIds.contains(member.getId())) {
                return null;
            }

            // targetCoupon을 재사용하여 쿠폰 발급
            return MemberCoupon.createMemberCoupon(member, targetCoupon);
        };
    }

    // Writer: 생성된 MemberCoupon 정보 DB 저장
    @Bean
    public ItemWriter<MemberCoupon> dormantMemberWriter() {
        return chunk -> {
            log.info(" 컴백 쿠폰 DB 저장 시작 (총 {}명)", chunk.size());
            memberCouponRepository.saveAll(chunk.getItems());
            log.info(" 청크 단위 쿠폰 지급 완료");
        };
    }
}
