package com.lbg0146.shop_service.coupon.repository;

import com.lbg0146.shop_service.coupon.entity.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

    List<MemberCoupon> findAllByMemberId(Long memberId);

    Optional<MemberCoupon> findByIdAndMemberId(Long memberCouponId, Long memberId);

    boolean existsByMemberIdAndCouponId(Long memberId, Long couponId);

    @Query("""
    SELECT mc.member.id
    FROM MemberCoupon mc
    WHERE mc.coupon.id = :couponId
    """)
    Set<Long> findMemberIdsByCouponId(@Param("couponId") Long couponId);
}