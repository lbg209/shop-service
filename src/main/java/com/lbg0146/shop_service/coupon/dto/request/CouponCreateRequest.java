package com.lbg0146.shop_service.coupon.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CouponCreateRequest(

        @NotBlank(message = "쿠폰명은 필수입니다.")
        String couponName,

        @NotBlank(message = "할인 방식은 필수입니다.")
        String discountType,

        @NotNull(message = "할인 값은 필수입니다.")
        @Positive(message = "할인 값은 1 이상이어야 합니다.")
        Integer discountValue,

        @NotNull(message = "최소 주문 금액은 필수입니다.")
        @PositiveOrZero(message = "최소 주문 금액은 0 이상이어야 합니다.")
        Long minOrderAmount,

        @Positive(message = "최대 할인 금액은 1 이상이어야 합니다.")
        Long maxDiscountAmount,

        @NotNull(message = "사용 가능 기간은 필수입니다.")
        @Positive(message = "사용 가능 기간은 1일 이상이어야 합니다.")
        Integer validDays

) {
}