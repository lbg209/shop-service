package com.lbg0146.shop_service.product.controller;

import com.lbg0146.shop_service.product.dto.request.ProductCreateRequest;
import com.lbg0146.shop_service.product.dto.request.ProductUpdateRequest;
import com.lbg0146.shop_service.product.dto.response.ProductResponse;
import com.lbg0146.shop_service.product.entity.ProductStatus;
import com.lbg0146.shop_service.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Long> createProduct(@RequestBody ProductCreateRequest request) {
        return ResponseEntity.ok(
                productService.createProduct(request)
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> findProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(
                productService.findProduct(productId)
        );
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> findProducts() {
        return ResponseEntity.ok(
                productService.findProducts()
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> findProductsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(
                productService.findProductsByCategory(categoryId)
        );
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Void> updateProduct(@PathVariable Long productId, @RequestBody ProductUpdateRequest request) {

        productService.updateProduct(productId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {

        productService.deleteProduct(productId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{productId}/status")
    public ResponseEntity<Void> changeProductStatus(@PathVariable Long productId, @RequestParam ProductStatus status) {

        productService.changeProductStatus(productId, status);
        return ResponseEntity.ok().build();
    }

}
