package com.lbg0146.shop_service.cart.dto.response;

public record CartItemResponse(
        Long cartItemId,
        Long productId,
        String productName,
        Long price,
        Integer quantity
) {
}
