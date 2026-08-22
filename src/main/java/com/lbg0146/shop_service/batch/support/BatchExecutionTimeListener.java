package com.lbg0146.shop_service.batch.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchExecutionTimeListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        jobExecution.getExecutionContext()
                .putLong("startTime", System.currentTimeMillis());

        log.info("===== Batch 시작 =====");
        log.info("Job: {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long startTime = jobExecution.getExecutionContext()
                .getLong("startTime");

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        log.info("===== Batch 종료 =====");
        log.info("Job: {}", jobExecution.getJobInstance().getJobName());
        log.info("상태: {}", jobExecution.getStatus());
        log.info("실행 시간: {} ms ({}초)",
                elapsedTime,
                elapsedTime / 1000.0);
    }
}
