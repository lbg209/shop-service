package com.lbg0146.shop_service.cart.dto.request;

public record CartItemCreateRequest(
        Long productId,
        Integer quantity
) {
}