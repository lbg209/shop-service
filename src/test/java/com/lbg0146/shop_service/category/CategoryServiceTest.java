package com.lbg0146.shop_service.category;

import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.product.dto.request.CategoryCreateRequest;
import com.lbg0146.shop_service.product.dto.request.CategoryUpdateRequest;
import com.lbg0146.shop_service.product.dto.response.CategoryResponse;
import com.lbg0146.shop_service.product.entity.Category;
import com.lbg0146.shop_service.product.repository.CategoryRepository;
import com.lbg0146.shop_service.product.service.CategoryService;
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
class CategoryServiceTest {

    @Autowired
    CategoryService categoryService;

    @Autowired
    CategoryRepository categoryRepository;

    @Test
    void 최상위_카테고리_등록() {

        CategoryCreateRequest request =
                new CategoryCreateRequest(
                        "테스트_전자제품",
                        null
                );

        Long categoryId = categoryService.createCategory(request);

        Category category = categoryRepository.findById(categoryId).orElseThrow();

        assertThat(category.getCategoryName()).isEqualTo("테스트_전자제품");

        assertThat(category.getParentCategory()).isNull();
    }

    @Test
    void 하위_카테고리_등록() {

        Category parent = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null)
        );

        CategoryCreateRequest request =
                new CategoryCreateRequest(
                        "테스트_노트북",
                        parent.getId()
                );

        Long categoryId = categoryService.createCategory(request);

        Category category = categoryRepository.findById(categoryId).orElseThrow();

        assertThat(category.getCategoryName()).isEqualTo("테스트_노트북");

        assertThat(category.getParentCategory().getId()).isEqualTo(parent.getId());
    }

    @Test
    void 존재하지_않는_부모_카테고리로_등록하면_실패() {

        CategoryCreateRequest request =
                new CategoryCreateRequest(
                        "테스트_휴대폰",
                        9999L
                );

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 카테고리_단건_조회() {

        Category category = categoryRepository.save(
                Category.createCategory("테스트_전자제품", null)
        );

        CategoryResponse response = categoryService.findCategory(category.getId());

        assertThat(response.categoryId()).isEqualTo(category.getId());

        assertThat(response.categoryName()).isEqualTo("테스트_전자제품");

        assertThat(response.parentCategoryId()).isNull();
    }

    @Test
    void 존재하지_않는_카테고리_조회_실패() {

        assertThatThrownBy(() ->
                categoryService.findCategory(9999L)
        )
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 최상위_카테고리_목록_조회() {

        Category electronics = categoryRepository.save(
                        Category.createCategory("테스트_전자제품", null));

        Category clothing = categoryRepository.save(
                        Category.createCategory("테스트_의류", null));

        categoryRepository.save(Category.createCategory("테스트_노트북", electronics));

        List<CategoryResponse> categories = categoryService.findRootCategories();

        List<String> categoryNames = categories.stream()
                .map(CategoryResponse::categoryName)
                .toList();

        assertThat(categoryNames)
                .contains("테스트_전자제품", "테스트_의류")
                .doesNotContain("테스트_노트북");
    }

    @Test
    void 하위_카테고리_목록_조회() {

        // given
        Category parent = categoryRepository.save(
                        Category.createCategory("테스트_전자제품", null));

        Category laptop = categoryRepository.save(
                        Category.createCategory("테스트_노트북", parent));

        Category tablet = categoryRepository.save(
                        Category.createCategory("테스트_태블릿", parent));

        Category otherParent = categoryRepository.save(
                        Category.createCategory("테스트_의류", null));

        categoryRepository.save(Category.createCategory("테스트_상의", otherParent));

        List<CategoryResponse> children = categoryService.findChildren(parent.getId());

        List<String> categoryNames = children.stream()
                .map(CategoryResponse::categoryName)
                .toList();

        assertThat(categoryNames)
                .containsExactlyInAnyOrder(
                        "테스트_노트북",
                        "테스트_태블릿"
                )
                .doesNotContain("테스트_상의");
    }

    @Test
    void 카테고리_수정() {

        Category category = categoryRepository.save(
                        Category.createCategory("테스트_전자제품", null));

        CategoryUpdateRequest request =
                new CategoryUpdateRequest("테스트_디지털기기");

        categoryService.updateCategory(category.getId(), request);

        Category updatedCategory = categoryRepository.findById(category.getId()).orElseThrow();

        assertThat(updatedCategory.getCategoryName()).isEqualTo("테스트_디지털기기");
    }

    @Test
    void 존재하지_않는_카테고리_수정_실패() {

        CategoryUpdateRequest request = new CategoryUpdateRequest("테스트_카테고리");

        assertThatThrownBy(() -> categoryService.updateCategory(9999L, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 카테고리_삭제() {

        Category category = categoryRepository.save(
                        Category.createCategory("테스트_전자제품", null));

        Long categoryId = category.getId();

        categoryService.deleteCategory(categoryId);

        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }

    @Test
    void 하위_카테고리가_있으면_부모_카테고리_삭제_실패() {

        Category parent = categoryRepository.save(
                        Category.createCategory("테스트_전자제품", null));

        categoryRepository.save(Category.createCategory("테스트_노트북", parent));

        assertThatThrownBy(() -> categoryService.deleteCategory(parent.getId()))
                .isInstanceOf(BusinessException.class);
    }
}