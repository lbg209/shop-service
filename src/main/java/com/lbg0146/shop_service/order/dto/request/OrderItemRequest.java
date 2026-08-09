package com.lbg0146.shop_service.order.dto.request;

public record OrderItemRequest(
        Long productId,
        Integer quantity
) {
}