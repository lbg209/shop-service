package com.lbg0146.shop_service.product.dto.response;

import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.product.entity.ProductStatus;

public record ProductResponse(
        Long productId,
        Long categoryId,
        String categoryName,
        String productName,
        Long price,
        Integer stockQuantity,
        String description,
        ProductStatus status
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getCategoryName(),
                product.getProductName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getDescription(),
                product.getStatus()
        );
    }
}