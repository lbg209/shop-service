package com.lbg0146.shop_service.batch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BatchTestController {

    private final JobOperator jobOperator;
    private final Job gradeUpdateJob; // Job 자체를 주입 !
    private final Job dormantMemberCouponJob;

    @GetMapping("/admin/batch/grade-update")
    public String runGradeUpdateBatch(
            // 외부에서 파라미터로 입력받도록 수정 (입력 안 하면 null)
            @RequestParam(value = "targetMonth", required = false) String targetMonth) throws Exception {

        JobParametersBuilder builder = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis());

        // targetMonth 값이 들어왔다면 파라미터에 추가!
        if (targetMonth != null && !targetMonth.isBlank()) {
            builder.addString("targetMonth", targetMonth);
            log.info("관리자 수동 배치 실행 요청! (타겟 월: {})", targetMonth);
        } else {
            log.info("관리자 수동 배치 실행 요청! (타겟 월: 파라미터 없음 -> 기본값 '지난달'로 실행)");
        }

        jobOperator.start(gradeUpdateJob, builder.toJobParameters());

        return "등급 갱신 배치 실행 완료! (타겟 월: " + (targetMonth != null ? targetMonth : "지난달 기본값") + ")";
    }

    // 3개월 미주문 휴면 고객 쿠폰 지급 수동 실행
    @GetMapping("/admin/batch/dormant-coupon")
    public String runDormantCouponBatch() throws Exception {

        log.info("관리자 수동 휴면 고객 쿠폰 지급 배치 실행 요청!");

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobOperator.start(dormantMemberCouponJob, jobParameters);

        return "3개월 미주문 휴면 고객 전용 쿠폰 지급 배치 실행 완료!";
    }
}
