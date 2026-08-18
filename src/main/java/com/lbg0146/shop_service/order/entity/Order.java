package com.lbg0146.shop_service.order.entity;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.entity.BaseEntity;
import com.lbg0146.shop_service.coupon.entity.MemberCoupon;
import com.lbg0146.shop_service.delivery.entity.Delivery;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_coupon_id")
    private MemberCoupon memberCoupon;

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

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Payment payment;

    @Column(nullable = false)
    private Long totalPrice;       // 상품 금액 합계

    @Column(nullable = false)
    private Long gradeDiscountAmount;   // 회원 등급 할인 금액

    @Column(nullable = false)
    private Long couponDiscountAmount;   // 쿠폰 할인 금액

    @Column(nullable = false)
    private Long discountAmount;        // 전체 할인 금액

    @Column(nullable = false)
    private Long finalPrice;       // 실제 결제 금액

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    public static Order createOrder(
            String orderNumber,
            Member member,
            CommonCodeDetail orderStatus,
            MemberCoupon memberCoupon,
            String receiverName,
            String receiverPhone,
            String zipCode,
            String address,
            String detailAddress,
            Long totalPrice,
            Long gradeDiscountAmount,
            Long couponDiscountAmount,
            Long discountAmount,
            Long finalPrice
    ) {
        Order order = new Order();

        order.orderNumber = orderNumber;
        order.member = member;
        order.orderStatus = orderStatus;
        order.memberCoupon = memberCoupon;
        order.receiverName = receiverName;
        order.receiverPhone = receiverPhone;
        order.zipCode = zipCode;
        order.address = address;
        order.detailAddress = detailAddress;
        order.totalPrice = totalPrice;
        order.gradeDiscountAmount = gradeDiscountAmount;
        order.couponDiscountAmount = couponDiscountAmount;
        order.discountAmount = discountAmount;
        order.finalPrice = finalPrice;
        order.orderedAt = LocalDateTime.now();

        return order;
    }

    public void changeStatus(CommonCodeDetail orderStatus) {
        this.orderStatus = orderStatus;
    }

    public boolean isPaid() {
        return "ORDER_STATUS".equals(orderStatus.getGroup().getGroupCode())
                && "PAID".equals(orderStatus.getCodeValue());
    }
}
