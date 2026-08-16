package com.lbg0146.shop_service.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DormantCouponScheduler {

    private final JobOperator jobOperator;
    private final Job dormantMemberCouponJob;

    // 크론 표현식 변경: 매주 월요일 새벽 3시 0분 0초에 실행
    @Scheduled(cron = "0 0 3 * * MON", zone = "Asia/Seoul")
    public void runDormantCouponBatch() throws Exception {

        log.info("===== [주간] 3개월 미주문 휴면 고객 쿠폰 지급 배치 시작 =====");

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobOperator.start(dormantMemberCouponJob, jobParameters);

        log.info("===== [주간] 3개월 미주문 휴면 고객 쿠폰 지급 배치 실행 요청 완료 =====");
    }
}
