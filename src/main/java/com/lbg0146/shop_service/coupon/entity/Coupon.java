package com.lbg0146.shop_service.coupon.entity;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    // 할인 방식 (RATE, AMOUNT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_type_code_id", nullable = false)
    private CommonCodeDetail discountType;

    @Column(nullable = false, length = 100)
    private String couponName;

    // RATE : 할인율 (10 = 10%)
    // AMOUNT : 할인금액 (5000 = 5000원)
    @Column(nullable = false)
    private Integer discountValue;

    // 쿠폰 사용 최소 주문 금액
    @Column(name = "min_order_amount", nullable = false)
    private Long minOrderAmount;

    // 정률 할인 최대 할인 금액 제한
    @Column(name = "max_discount_amount")
    private Long maxDiscountAmount;

    // 발급 후 사용 가능 기간(일)
    @Column(nullable = false)
    private Integer validDays;

    // 쿠폰 정책 삭제 여부 (Soft Delete)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Coupon createCoupon(
            CommonCodeDetail discountType,
            String couponName,
            Integer discountValue,
            Long minOrderAmount,
            Long maxDiscountAmount,
            Integer validDays
    ) {
        Coupon coupon = new Coupon();

        coupon.discountType = discountType;
        coupon.couponName = couponName;
        coupon.discountValue = discountValue;
        coupon.minOrderAmount = minOrderAmount;
        coupon.maxDiscountAmount = maxDiscountAmount;
        coupon.validDays = validDays;

        return coupon;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
