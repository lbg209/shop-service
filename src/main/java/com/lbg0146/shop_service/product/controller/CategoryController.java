package com.lbg0146.shop_service.product.controller;

import com.lbg0146.shop_service.product.dto.request.CategoryCreateRequest;
import com.lbg0146.shop_service.product.dto.request.CategoryUpdateRequest;
import com.lbg0146.shop_service.product.dto.response.CategoryResponse;
import com.lbg0146.shop_service.product.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Long> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        Long categoryId = categoryService.createCategory(request);

        return ResponseEntity.ok(categoryId);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> findCategory(@PathVariable Long categoryId) {

        return ResponseEntity.ok(
                categoryService.findCategory(categoryId)
        );
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findRootCategories() {

        return ResponseEntity.ok(
                categoryService.findRootCategories()
        );
    }

    @GetMapping("/{categoryId}/children")
    public ResponseEntity<List<CategoryResponse>> findChildren(@PathVariable Long categoryId) {
        return ResponseEntity.ok(
                categoryService.findChildren(categoryId)
        );
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<Void> updateCategory(@PathVariable Long categoryId, @Valid @RequestBody CategoryUpdateRequest request) {
        categoryService.updateCategory(categoryId, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);

        return ResponseEntity.ok().build();
    }

}
