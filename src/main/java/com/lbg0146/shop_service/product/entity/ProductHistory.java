package com.lbg0146.shop_service.product.entity;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.entity.BaseHistoryEntity;
import com.lbg0146.shop_service.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductHistory extends BaseHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_type_code_id", nullable = false)
    private CommonCodeDetail changeType;

    @Column(nullable = false, length = 100)
    private String productName;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(length = 500)
    private String description;

    // 상품 상태 스냅샷
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    // 변경한 관리자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private Member changedBy;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    private LocalDateTime validTo;

    public static ProductHistory create(
            Product product,
            CommonCodeDetail changeType,
            Member changedBy,
            LocalDateTime validFrom,
            LocalDateTime validTo
    ) {
        ProductHistory history = new ProductHistory();

        history.product = product;
        history.changeType = changeType;
        history.productName = product.getProductName();
        history.price = product.getPrice();
        history.stockQuantity = product.getStockQuantity();
        history.description = product.getDescription();
        history.status = product.getStatus();
        history.changedBy = changedBy;
        history.validFrom = validFrom;
        history.validTo = validTo;

        return history;
    }

    public void close(LocalDateTime validTo) {
        this.validTo = validTo;
    }
}
