package com.lbg0146.shop_service.payment.dto.request;

public record PaymentCreateRequest(
        Long orderId,
        String paymentMethod
) {
}