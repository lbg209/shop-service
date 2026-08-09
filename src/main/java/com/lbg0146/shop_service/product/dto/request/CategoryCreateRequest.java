package com.lbg0146.shop_service.product.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequest(
        @NotBlank(message = "카테고리명은 필수입니다.")
        String categoryName,

        Long parentCategoryId
) {
}