package com.lbg0146.shop_service.product.repository;

import com.lbg0146.shop_service.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndDeletedAtIsNull(Long productId);

    List<Product> findAllByDeletedAtIsNull();

    List<Product> findAllByCategoryIdAndDeletedAtIsNull(Long categoryId);

    // 비관적 쓰기 락을 적용한 상품 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId AND p.deletedAt IS NULL")
    Optional<Product> findByIdAndDeletedAtIsNullWithLock(@Param("productId") Long productId);
}
