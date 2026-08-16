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
public class GradeUpdateScheduler {

    private final JobOperator jobOperator;
    private final Job gradeUpdateJob;

    @Scheduled(cron = "0 0 2 1 * *", zone = "Asia/Seoul")
    public void runGradeUpdateBatch() throws Exception {

        log.info("===== 회원 등급 자동 갱신 배치 시작 =====");

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobOperator.start(
                gradeUpdateJob,
                jobParameters
        );

        log.info("===== 회원 등급 자동 갱신 배치 실행 요청 완료 =====");
    }
}
