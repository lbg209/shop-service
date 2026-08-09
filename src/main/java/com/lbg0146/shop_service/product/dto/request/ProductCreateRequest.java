package com.lbg0146.shop_service.product.dto.request;

public record ProductCreateRequest(
        Long categoryId,
        String productName,
        Long price,
        Integer stockQuantity,
        String description
) {
}