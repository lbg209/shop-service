package com.lbg0146.shop_service.order.entity;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.entity.BaseEntity;
import com.lbg0146.shop_service.delivery.entity.Delivery;
import com.lbg0146.shop_service.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_status_code_id", nullable = false)
    private CommonCodeDetail orderStatus;

    @Column(nullable = false, length = 50)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String receiverPhone;

    @Column(nullable = false, length = 10)
    private String zipCode;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 255)
    private String detailAddress;

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Delivery delivery;

    @Column(nullable = false)
    private Long totalPrice;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    public static Order createOrder(
            String orderNumber,
            Member member,
            CommonCodeDetail orderStatus,
            String receiverName,
            String receiverPhone,
            String zipCode,
            String address,
            String detailAddress,
            Long totalPrice
    ) {
        Order order = new Order();

        order.orderNumber = orderNumber;
        order.member = member;
        order.orderStatus = orderStatus;
        order.receiverName = receiverName;
        order.receiverPhone = receiverPhone;
        order.zipCode = zipCode;
        order.address = address;
        order.detailAddress = detailAddress;
        order.totalPrice = totalPrice;
        order.orderedAt = LocalDateTime.now();

        return order;
    }
}
