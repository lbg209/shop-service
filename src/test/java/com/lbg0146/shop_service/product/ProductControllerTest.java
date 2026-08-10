package com.lbg0146.shop_service.product;

import com.lbg0146.shop_service.product.dto.request.ProductCreateRequest;
import com.lbg0146.shop_service.product.dto.request.ProductUpdateRequest;
import com.lbg0146.shop_service.product.entity.Category;
import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.product.entity.ProductStatus;
import com.lbg0146.shop_service.product.repository.CategoryRepository;
import com.lbg0146.shop_service.product.repository.ProductRepository;
import com.lbg0146.shop_service.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductService productService;

    @Test
    void 상품_등록_API_성공() throws Exception {

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

        mockMvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void 상품_단건_조회_API_성공() throws Exception {

        Category category = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null));

        Product product = productRepository.save(
                Product.createProduct(
                        category,
                        "테스트_노트북",
                        1500000L,
                        10,
                        "테스트 상품입니다."
                )
        );

        mockMvc.perform(
                        get("/api/products/{productId}", product.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$.categoryId")
                        .value(category.getId()))
                .andExpect(jsonPath("$.categoryName")
                        .value("테스트_전자제품"))
                .andExpect(jsonPath("$.productName")
                        .value("테스트_노트북"))
                .andExpect(jsonPath("$.price")
                        .value(1500000))
                .andExpect(jsonPath("$.stockQuantity")
                        .value(10))
                .andExpect(jsonPath("$.status")
                        .value("SALE"));
    }

    @Test
    void 존재하지_않는_상품_조회_API_실패() throws Exception {

        mockMvc.perform(
                get("/api/products/{productId}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void 상품_전체_조회_API_성공() throws Exception {

        Category category = categoryRepository.save(Category.createCategory("테스트_전자제품", null));

        categoryRepository.save(Category.createCategory("테스트_의류", null));

        productRepository.save(
                Product.createProduct(
                        category,
                        "테스트_노트북",
                        1500000L,
                        10,
                        "테스트 노트북"
                )
        );

        productRepository.save(
                Product.createProduct(
                        category,
                        "테스트_태블릿",
                        800000L,
                        20,
                        "테스트 태블릿"
                )
        );

        mockMvc.perform(
                        get("/api/products")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].productName")
                        .value(org.hamcrest.Matchers.hasItems(
                                "테스트_노트북",
                                "테스트_태블릿"
                        )));
    }

    @Test
    void 삭제된_상품은_전체_조회에서_제외() throws Exception {

        Category category = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null));

        Product product = productRepository.save(
                Product.createProduct(
                        category,
                        "테스트_삭제상품",
                        100000L,
                        10,
                        "삭제 테스트"
                )
        );

        productService.deleteProduct(product.getId());

        mockMvc.perform(
                        get("/api/products")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].productName")
                        .value(org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.hasItem("테스트_삭제상품")
                        )));
    }

    @Test
    void 카테고리별_상품_조회_API_성공() throws Exception {

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
                        20,
                        "테스트 셔츠"
                )
        );

        mockMvc.perform(
                        get("/api/products/category/{categoryId}",
                                electronics.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].productName")
                        .value(org.hamcrest.Matchers.hasItem(
                                "테스트_노트북"
                        )))
                .andExpect(jsonPath("$[*].productName")
                        .value(org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.hasItem(
                                        "테스트_셔츠"
                                )
                        )));
    }

    @Test
    void 존재하지_않는_카테고리_상품_조회_API_실패() throws Exception {

        mockMvc.perform(
                        get("/api/products/category/{categoryId}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void 상품_수정_API_성공() throws Exception {

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

        mockMvc.perform(
                        put("/api/products/{productId}", product.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();

        assertThat(updatedProduct.getCategory().getId()).isEqualTo(newCategory.getId());

        assertThat(updatedProduct.getProductName()).isEqualTo("수정된_노트북");

        assertThat(updatedProduct.getPrice()).isEqualTo(1800000L);

        assertThat(updatedProduct.getStockQuantity()).isEqualTo(20);
    }

    @Test
    void 존재하지_않는_상품_수정_API_실패() throws Exception {

        Category category = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null)
        );

        ProductUpdateRequest request =
                new ProductUpdateRequest(
                        category.getId(),
                        "수정된_상품",
                        10000L,
                        10,
                        "수정된 설명"
                );

        mockMvc.perform(
                        put("/api/products/{productId}", 9999L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void 상품_상태_변경_API_성공() throws Exception {

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

        mockMvc.perform(
                        patch("/api/products/{productId}/status",
                                product.getId())
                                .param("status", "SOLD_OUT")
                )
                .andExpect(status().isOk());

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();

        assertThat(updatedProduct.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
    }

    @Test
    void 존재하지_않는_상품_상태_변경_API_실패() throws Exception {

        mockMvc.perform(
                        patch("/api/products/{productId}/status", 9999L)
                                .param("status", "SOLD_OUT")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void 상품_삭제_API_성공() throws Exception {

        Category category = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null));

        Product product = productRepository.save(
                Product.createProduct(
                        category,
                        "테스트_삭제상품",
                        100000L,
                        10,
                        "삭제 테스트"
                )
        );

        Long productId = product.getId();

        mockMvc.perform(
                        delete("/api/products/{productId}", productId)
                )
                .andExpect(status().isOk());

        // DB는 남아있고 deletedAt만 기록됐는지 확인
        Product deletedProduct = productRepository.findById(productId).orElseThrow();

        assertThat(deletedProduct.getDeletedAt()).isNotNull();

        // 일반 조회 API에서는 조회되지 않아야 함
        mockMvc.perform(
                        get("/api/products/{productId}", productId)
                )
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        get("/api/products")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].productName")
                        .value(org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.hasItem(
                                        "테스트_삭제상품"
                                )
                        )));
    }

}
