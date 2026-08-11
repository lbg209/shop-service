package com.lbg0146.shop_service.order.dto.request;

import java.util.List;

public record OrderCreateRequest(
        List<OrderItemRequest> items,
        String receiverName,
        String receiverPhone,
        String zipCode,
        String address,
        String detailAddress,
        Long memberCouponId
) {
}