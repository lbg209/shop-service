package com.lbg0146.shop_service.coupon.repository;

import com.lbg0146.shop_service.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByIdAndDeletedAtIsNull(Long id);
}