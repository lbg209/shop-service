package com.lbg0146.shop_service.payment.entity;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.entity.BaseEntity;
import com.lbg0146.shop_service.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // PAID, READY, FAILED, CANCEL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_status_code_id", nullable = false)
    private CommonCodeDetail paymentStatus;

    // CARD, CASH, TRANSFER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_code_id", nullable = false)
    private CommonCodeDetail paymentMethod;

    @Column(nullable = false)
    private Long paidAmount;

    // PG사 결제 승인 키
    @Column(length = 100)
    private String paymentKey;

    private LocalDateTime paidAt;

    @Column(length = 255)
    private String failReason;
}
