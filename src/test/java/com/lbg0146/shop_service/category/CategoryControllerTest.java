package com.lbg0146.shop_service.category;

import com.lbg0146.shop_service.product.dto.request.CategoryCreateRequest;
import com.lbg0146.shop_service.product.dto.request.CategoryUpdateRequest;
import com.lbg0146.shop_service.product.entity.Category;
import com.lbg0146.shop_service.product.repository.CategoryRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CategoryRepository categoryRepository;

    @Test
    void 카테고리_등록_API_성공() throws Exception {

        CategoryCreateRequest request =
                new CategoryCreateRequest(
                        "테스트_전자제품",
                        null
                );

        mockMvc.perform(
                        post("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void 카테고리_단건_조회_API_성공() throws Exception {

        Category category = categoryRepository.save(Category.createCategory("테스트_전자제품", null));

        mockMvc.perform(
                        get("/api/categories/{categoryId}", category.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId")
                        .value(category.getId()))
                .andExpect(jsonPath("$.categoryName")
                        .value("테스트_전자제품"))
                .andExpect(jsonPath("$.parentCategoryId")
                        .isEmpty());
    }

    @Test
    void 최상위_카테고리_목록_API_조회() throws Exception {

        categoryRepository.save(Category.createCategory("테스트_전자제품", null));

        categoryRepository.save(Category.createCategory("테스트_의류", null));

        Category parent = categoryRepository.save(Category.createCategory("테스트_가전제품", null));

        categoryRepository.save(Category.createCategory("테스트_냉장고", parent));

        mockMvc.perform(
                        get("/api/categories")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].categoryName")
                        .value(org.hamcrest.Matchers.hasItems(
                                "테스트_전자제품",
                                "테스트_의류",
                                "테스트_가전제품"
                        )))
                .andExpect(jsonPath("$[*].categoryName")
                        .value(org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.hasItem("테스트_냉장고")
                        )));
    }

    @Test
    void 하위_카테고리_목록_API_조회() throws Exception {

        Category parent = categoryRepository.save(Category.createCategory("테스트_전자제품", null));

        categoryRepository.save(Category.createCategory("테스트_노트북", parent));

        categoryRepository.save(Category.createCategory("테스트_태블릿", parent));

        Category otherParent = categoryRepository.save(Category.createCategory("테스트_의류", null));

        categoryRepository.save(Category.createCategory("테스트_셔츠", otherParent));

        mockMvc.perform(
                        get("/api/categories/{categoryId}/children",
                                parent.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].categoryName")
                        .value(org.hamcrest.Matchers.hasItems(
                                "테스트_노트북",
                                "테스트_태블릿"
                        )))
                .andExpect(jsonPath("$[*].categoryName")
                        .value(org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.hasItem("테스트_셔츠")
                        )));
    }

    @Test
    void 카테고리_수정_API_성공() throws Exception {

        Category category = categoryRepository.save(Category.createCategory("테스트_전자제품", null));

        CategoryUpdateRequest request = new CategoryUpdateRequest("테스트_디지털기기");

        mockMvc.perform(
                        put("/api/categories/{categoryId}", category.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        Category updatedCategory = categoryRepository.findById(category.getId()).orElseThrow();

        assertThat(updatedCategory.getCategoryName()).isEqualTo("테스트_디지털기기");
    }

    @Test
    void 카테고리_삭제_API_성공() throws Exception {

        Category category = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null)
        );

        mockMvc.perform(
                        delete("/api/categories/{categoryId}", category.getId())
                )
                .andExpect(status().isOk());

        assertThat(categoryRepository.findById(category.getId())).isEmpty();
    }

    @Test
    void 하위_카테고리가_있으면_부모_삭제_API_실패() throws Exception {

        Category parent = categoryRepository.save(Category.createCategory("테스트_전자제품", null));

        categoryRepository.save(Category.createCategory("테스트_노트북", parent));

        mockMvc.perform(
                        delete("/api/categories/{categoryId}", parent.getId())
                )
                .andExpect(status().isBadRequest());
    }
}
