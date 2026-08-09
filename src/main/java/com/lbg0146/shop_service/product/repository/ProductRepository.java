package com.lbg0146.shop_service.product.repository;

import com.lbg0146.shop_service.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndDeletedAtIsNull(Long productId);

    List<Product> findAllByDeletedAtIsNull();

    List<Product> findAllByCategoryIdAndDeletedAtIsNull(Long categoryId);
}
