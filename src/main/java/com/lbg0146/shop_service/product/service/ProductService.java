package com.lbg0146.shop_service.product.service;

import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.product.dto.request.ProductCreateRequest;
import com.lbg0146.shop_service.product.dto.request.ProductUpdateRequest;
import com.lbg0146.shop_service.product.dto.response.ProductResponse;
import com.lbg0146.shop_service.product.entity.Category;
import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.product.entity.ProductStatus;
import com.lbg0146.shop_service.product.repository.CategoryRepository;
import com.lbg0146.shop_service.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductHistoryService productHistoryService;

    @Transactional
    public Long createProduct(ProductCreateRequest request) {

        Category category = categoryRepository.findById(request.categoryId()).orElseThrow(() ->
                        new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        Product product = Product.createProduct(
                category,
                request.productName(),
                request.price(),
                request.stockQuantity(),
                request.description()
        );

        Product savedProduct = productRepository.save(product);

        productHistoryService.saveHistory(
                savedProduct,
                "CREATE",
                null
        );

        return savedProduct.getId();
    }

    public ProductResponse findProduct(Long productId) {

        Product product = productRepository.findByIdAndDeletedAtIsNull(productId).orElseThrow(() ->
                        new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        return ProductResponse.from(product);
    }

    public List<ProductResponse> findProducts() {

        return productRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    public List<ProductResponse> findProductsByCategory(Long categoryId) {

        if (!categoryRepository.existsById(categoryId)) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        return productRepository.findAllByCategoryIdAndDeletedAtIsNull(categoryId)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional
    public void updateProduct(Long productId, ProductUpdateRequest request) {

        Product product = productRepository.findByIdAndDeletedAtIsNull(productId).orElseThrow(() ->
                        new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Category category = categoryRepository.findById(request.categoryId()).orElseThrow(() ->
                        new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        productHistoryService.closeCurrentHistory(
                productId,
                LocalDateTime.now()
        );

        product.update(
                category,
                request.productName(),
                request.price(),
                request.stockQuantity(),
                request.description()
        );

        productHistoryService.saveHistory(
                product,
                "UPDATE",
                null
        );
    }

    @Transactional
    public void deleteProduct(Long productId) {

        Product product = productRepository.findByIdAndDeletedAtIsNull(productId).orElseThrow(() ->
                        new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        productHistoryService.closeCurrentHistory(
                productId,
                now
        );

        product.delete();

        productHistoryService.saveHistory(
                product,
                "DELETE",
                null
        );
    }

    @Transactional
    public void changeProductStatus(Long productId, ProductStatus status) {

        Product product = productRepository.findByIdAndDeletedAtIsNull(productId).orElseThrow(() ->
                        new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        productHistoryService.closeCurrentHistory(
                productId,
                LocalDateTime.now()
        );

        product.changeStatus(status);

        productHistoryService.saveHistory(
                product,
                "UPDATE",
                null
        );
    }
}
