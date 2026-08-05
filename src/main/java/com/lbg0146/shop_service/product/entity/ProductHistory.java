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

    // 판매 상태 당시 값 저장
    @Column(nullable = false, length = 20)
    private String status;

    // 변경한 관리자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private Member changedBy;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    private LocalDateTime validTo;
}
