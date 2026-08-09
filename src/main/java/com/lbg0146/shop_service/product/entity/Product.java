package com.lbg0146.shop_service.product.entity;

import com.lbg0146.shop_service.common.entity.BaseEntity;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String productName;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Product createProduct(
            Category category,
            String productName,
            Long price,
            Integer stockQuantity,
            String description
    ) {
        Product product = new Product();

        product.category = category;
        product.productName = productName;
        product.price = price;
        product.stockQuantity = stockQuantity;
        product.description = description;
        product.status = ProductStatus.SALE;

        return product;
    }

    public void update(
            Category category,
            String productName,
            Long price,
            Integer stockQuantity,
            String description
    ) {
        this.category = category;
        this.productName = productName;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.description = description;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void changeStatus(ProductStatus status) {
        this.status = status;
    }
    // 동시성 문제 발생 !!!!!
    public void decreaseStock(Integer quantity) {

        if (stockQuantity < quantity) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }

        stockQuantity -= quantity;

        if (stockQuantity == 0) {
            status = ProductStatus.SOLD_OUT;
        }
    }
}
