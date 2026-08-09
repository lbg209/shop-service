package com.lbg0146.shop_service.product.repository;

import com.lbg0146.shop_service.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByParentCategoryIsNull();

    List<Category> findAllByParentCategoryId(Long parentCategoryId);

    boolean existsByParentCategoryId(Long parentCategoryId);
}
