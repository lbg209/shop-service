package com.lbg0146.shop_service.product;

import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.product.dto.request.ProductCreateRequest;
import com.lbg0146.shop_service.product.dto.request.ProductUpdateRequest;
import com.lbg0146.shop_service.product.dto.response.ProductResponse;
import com.lbg0146.shop_service.product.entity.Category;
import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.product.entity.ProductStatus;
import com.lbg0146.shop_service.product.repository.CategoryRepository;
import com.lbg0146.shop_service.product.repository.ProductRepository;
import com.lbg0146.shop_service.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ProductServiceTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductService productService;

    @Test
    void 상품_등록() {

        Category category = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null)
        );

        ProductCreateRequest request =
                new ProductCreateRequest(
                        category.getId(),
                        "테스트_노트북",
                        1500000L,
                        10,
                        "테스트 상품입니다."
                );

        Long productId = productService.createProduct(request);

        Product product = productRepository.findById(productId).orElseThrow();

        assertThat(product.getProductName()).isEqualTo("테스트_노트북");

        assertThat(product.getPrice()).isEqualTo(1500000L);

        assertThat(product.getStockQuantity()).isEqualTo(10);

        assertThat(product.getCategory().getId()).isEqualTo(category.getId());

        assertThat(product.getStatus()).isEqualTo(ProductStatus.SALE);
    }

    @Test
    void 존재하지_않는_카테고리로_상품_등록_실패() {

        ProductCreateRequest request =
                new ProductCreateRequest(
                        9999L,
                        "테스트_노트북",
                        1500000L,
                        10,
                        "테스트 상품입니다."
                );

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(BusinessException.class);

        assertThat(productRepository.findAllByDeletedAtIsNull()).noneMatch(product ->
                        product.getProductName().equals("테스트_노트북"));
    }

    @Test
    void 상품_단건_조회() {

        Category category = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null)
        );

        Product product = productRepository.save(
                Product.createProduct(
                        category,
                        "테스트_노트북",
                        1500000L,
                        10,
                        "테스트 상품입니다."
                )
        );

        ProductResponse response = productService.findProduct(product.getId());

        assertThat(response.productId()).isEqualTo(product.getId());

        assertThat(response.categoryId()).isEqualTo(category.getId());

        assertThat(response.categoryName()).isEqualTo("테스트_전자제품");

        assertThat(response.productName()).isEqualTo("테스트_노트북");

        assertThat(response.price()).isEqualTo(1500000L);

        assertThat(response.stockQuantity()).isEqualTo(10);

        assertThat(response.status()).isEqualTo(ProductStatus.SALE);
    }

    @Test
    void 존재하지_않는_상품_조회_실패() {

        assertThatThrownBy(() -> productService.findProduct(9999L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 상품_전체_조회() {

        Category category = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null)
        );

        productRepository.save(
                Product.createProduct(
                        category,
                        "테스트_노트북",
                        1500000L,
                        10,
                        "테스트 상품입니다."
                )
        );

        productRepository.save(
                Product.createProduct(
                        category,
                        "테스트_태블릿",
                        800000L,
                        20,
                        "테스트 태블릿입니다."
                )
        );

        List<ProductResponse> products = productService.findProducts();

        List<String> productNames = products.stream()
                .map(ProductResponse::productName)
                .toList();

        assertThat(productNames)
                .contains(
                        "테스트_노트북",
                        "테스트_태블릿"
                );
    }

    @Test
    void 카테고리별_상품_조회() {

        Category electronics = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null));

        Category clothing = categoryRepository.save(
                Category.createCategory("테스트_의류", null));

        productRepository.save(
                Product.createProduct(
                        electronics,
                        "테스트_노트북",
                        1500000L,
                        10,
                        "테스트 노트북"
                )
        );

        productRepository.save(
                Product.createProduct(
                        clothing,
                        "테스트_셔츠",
                        50000L,
                        30,
                        "테스트 셔츠"
                )
        );

        List<ProductResponse> products = productService.findProductsByCategory(electronics.getId());

        List<String> productNames = products.stream()
                .map(ProductResponse::productName)
                .toList();

        assertThat(productNames)
                .contains("테스트_노트북")
                .doesNotContain("테스트_셔츠");
    }

    @Test
    void 존재하지_않는_카테고리로_상품_조회_실패() {

        assertThatThrownBy(() -> productService.findProductsByCategory(9999L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 상품_수정() {

        Category oldCategory = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null));

        Category newCategory = categoryRepository.save(
                Category.createCategory("테스트_컴퓨터", null));

        Product product = productRepository.save(
                Product.createProduct(
                        oldCategory,
                        "테스트_노트북",
                        1500000L,
                        10,
                        "기존 상품입니다."
                )
        );

        ProductUpdateRequest request =
                new ProductUpdateRequest(
                        newCategory.getId(),
                        "수정된_노트북",
                        1800000L,
                        20,
                        "수정된 상품입니다."
                );

        productService.updateProduct(
                product.getId(),
                request
        );

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();

        assertThat(updatedProduct.getCategory().getId()).isEqualTo(newCategory.getId());

        assertThat(updatedProduct.getProductName()).isEqualTo("수정된_노트북");

        assertThat(updatedProduct.getPrice()).isEqualTo(1800000L);

        assertThat(updatedProduct.getStockQuantity()).isEqualTo(20);

        assertThat(updatedProduct.getDescription()).isEqualTo("수정된 상품입니다.");
    }

    @Test
    void 존재하지_않는_상품_수정_실패() {

        ProductUpdateRequest request =
                new ProductUpdateRequest(
                        1L,
                        "수정된_상품",
                        10000L,
                        10,
                        "수정된 설명"
                );

        assertThatThrownBy(() -> productService.updateProduct(9999L, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 존재하지_않는_카테고리로_상품_수정_실패() {

        Category category = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null));

        Product product = productRepository.save(
                Product.createProduct(
                        category,
                        "테스트_노트북",
                        1500000L,
                        10,
                        "테스트 상품"
                )
        );

        ProductUpdateRequest request =
                new ProductUpdateRequest(
                        9999L,
                        "수정된_노트북",
                        1800000L,
                        20,
                        "수정된 상품"
                );

        assertThatThrownBy(() -> productService.updateProduct(product.getId(), request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 상품_상태_변경() {

        Category category = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null));

        Product product = productRepository.save(
                Product.createProduct(
                        category,
                        "테스트_노트북",
                        1500000L,
                        10,
                        "테스트 상품"
                )
        );

        productService.changeProductStatus(product.getId(), ProductStatus.SOLD_OUT);

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();

        assertThat(updatedProduct.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
    }

    @Test
    void 존재하지_않는_상품_상태_변경_실패() {

        assertThatThrownBy(() -> productService.changeProductStatus(
                        9999L,
                        ProductStatus.SOLD_OUT
                )
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    void 상품_삭제() {

        Category category = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null));

        Product product = productRepository.save(
                Product.createProduct(
                        category,
                        "테스트_노트북",
                        1500000L,
                        10,
                        "테스트 상품"
                )
        );

        Long productId = product.getId();

        productService.deleteProduct(productId);

        Product deletedProduct = productRepository.findById(productId).orElseThrow();

        assertThat(deletedProduct.getDeletedAt()).isNotNull();

        assertThat(productRepository.findByIdAndDeletedAtIsNull(productId)).isEmpty();
    }

    @Test
    void 삭제된_상품_조회_실패() {

        Category category = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null));

        Product product = productRepository.save(
                Product.createProduct(
                        category,
                        "테스트_노트북",
                        1500000L,
                        10,
                        "테스트 상품"
                )
        );

        productService.deleteProduct(product.getId());

        assertThatThrownBy(() -> productService.findProduct(product.getId()))
                .isInstanceOf(BusinessException.class);
    }

}
