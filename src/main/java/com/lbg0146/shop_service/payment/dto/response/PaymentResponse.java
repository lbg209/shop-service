package com.lbg0146.shop_service.payment.dto.response;

import com.lbg0146.shop_service.payment.entity.Payment;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        String paymentStatus,
        String paymentMethod,
        Long paidAmount,
        String paymentKey,
        LocalDateTime paidAt,
        String failReason
) {

    public static PaymentResponse from(Payment payment) {
        if (payment == null) {
            return null;
        }

        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentStatus().getCodeValue(),
                payment.getPaymentMethod().getCodeValue(),
                payment.getPaidAmount(),
                payment.getPaymentKey(),
                payment.getPaidAt(),
                payment.getFailReason()
        );
    }
}