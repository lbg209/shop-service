package com.lbg0146.shop_service.product.service;

import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.product.dto.request.CategoryCreateRequest;
import com.lbg0146.shop_service.product.dto.request.CategoryUpdateRequest;
import com.lbg0146.shop_service.product.dto.response.CategoryResponse;
import com.lbg0146.shop_service.product.entity.Category;
import com.lbg0146.shop_service.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Long createCategory(CategoryCreateRequest request) {

        Category parentCategory = null;

        if (request.parentCategoryId() != null) {
            parentCategory = categoryRepository.findById(request.parentCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        Category category = Category.createCategory(
                request.categoryName(),
                parentCategory
        );

        Category savedCategory = categoryRepository.save(category);

        return savedCategory.getId();
    }

    public CategoryResponse findCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        return CategoryResponse.from(category);
    }

    public List<CategoryResponse> findRootCategories() {

        return categoryRepository.findAllByParentCategoryIsNull()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public List<CategoryResponse> findChildren(Long parentCategoryId) {

        return categoryRepository.findAllByParentCategoryId(parentCategoryId)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public void updateCategory(Long categoryId, CategoryUpdateRequest request) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        category.update(request.categoryName());
    }

    @Transactional
    public void deleteCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (categoryRepository.existsByParentCategoryId(categoryId)) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_CHILDREN);
        }

        categoryRepository.delete(category);
    }
}
