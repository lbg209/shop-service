package com.lbg0146.shop_service.coupon.repository;

import com.lbg0146.shop_service.coupon.entity.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

    List<MemberCoupon> findAllByMemberId(Long memberId);

    Optional<MemberCoupon> findByIdAndMemberId(Long memberCouponId, Long memberId);

    boolean existsByMemberIdAndCouponId(Long memberId, Long couponId);
}