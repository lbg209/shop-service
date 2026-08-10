package com.lbg0146.shop_service.coupon.dto.request;

import jakarta.validation.constraints.NotNull;

public record MemberCouponIssueRequest(

        @NotNull(message = "회원 ID는 필수입니다.")
        Long memberId,

        @NotNull(message = "쿠폰 ID는 필수입니다.")
        Long couponId
) {}
