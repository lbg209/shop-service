package com.lbg0146.shop_service.product.entity;

import com.lbg0146.shop_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String categoryName;

    // 상위 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    public static Category createCategory(String categoryName, Category parentCategory) {
        Category category = new Category();

        category.categoryName = categoryName;
        category.parentCategory = parentCategory;

        return category;
    }

    public void update(String categoryName) {
        this.categoryName = categoryName;
    }
}
