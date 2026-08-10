package com.lbg0146.shop_service.delivery.entity;

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
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_status_code_id", nullable = false)
    private CommonCodeDetail deliveryStatus;

    @Column(length = 100)
    private String trackingNumber;

    private LocalDateTime shippedAt;

    private LocalDateTime deliveredAt;

    public static Delivery createDelivery(
            Order order,
            CommonCodeDetail deliveryStatus
    ) {
        Delivery delivery = new Delivery();
        delivery.order = order;
        delivery.deliveryStatus = deliveryStatus;
        return delivery;
    }

    public void changeStatus(CommonCodeDetail deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public void startShipping() {
        this.shippedAt = LocalDateTime.now();
    }

    public void completeDelivery() {
        this.deliveredAt = LocalDateTime.now();
    }
}