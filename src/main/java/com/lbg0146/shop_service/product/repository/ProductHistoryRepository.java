package com.lbg0146.shop_service.product.repository;

import com.lbg0146.shop_service.product.entity.ProductHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductHistoryRepository extends JpaRepository<ProductHistory, Long> {

    List<ProductHistory> findAllByProductId(Long productId);

    Optional<ProductHistory> findTopByProductIdAndValidToIsNullOrderByValidFromDesc(
            Long productId
    );
}