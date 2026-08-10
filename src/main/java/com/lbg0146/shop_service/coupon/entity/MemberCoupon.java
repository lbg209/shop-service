package com.lbg0146.shop_service.coupon.entity;

import com.lbg0146.shop_service.common.entity.BaseEntity;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_coupon_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponStatus status;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    private LocalDateTime usedAt;

    private LocalDateTime expiredAt;

    public static MemberCoupon createMemberCoupon(
            Member member,
            Coupon coupon
    ) {
        MemberCoupon memberCoupon = new MemberCoupon();

        LocalDateTime now = LocalDateTime.now();

        memberCoupon.member = member;
        memberCoupon.coupon = coupon;
        memberCoupon.status = CouponStatus.ISSUED;
        memberCoupon.issuedAt = now;
        memberCoupon.expiredAt = now.plusDays(coupon.getValidDays());

        return memberCoupon;
    }

    public void use() {

        if (status != CouponStatus.ISSUED) {
            throw new BusinessException(ErrorCode.COUPON_NOT_USABLE);
        }

        if (expiredAt.isBefore(LocalDateTime.now())) {
            status = CouponStatus.EXPIRED;
            throw new BusinessException(ErrorCode.COUPON_EXPIRED);
        }

        status = CouponStatus.USED;
        usedAt = LocalDateTime.now();
    }

    // 테스트 메서드
    public void expire() {
        this.expiredAt = LocalDateTime.now().minusDays(1);
    }
}
