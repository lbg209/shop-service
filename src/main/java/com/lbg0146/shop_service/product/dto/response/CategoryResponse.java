package com.lbg0146.shop_service.product.dto.response;

import com.lbg0146.shop_service.product.entity.Category;

public record CategoryResponse(
        Long categoryId,
        String categoryName,
        Long parentCategoryId
) {
    public static CategoryResponse from(Category category) {

        return new CategoryResponse(
                category.getId(),
                category.getCategoryName(),
                category.getParentCategory() != null
                        ? category.getParentCategory().getId()
                        : null
        );
    }
}
