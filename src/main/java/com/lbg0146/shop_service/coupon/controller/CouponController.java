package com.lbg0146.shop_service.coupon.controller;

import com.lbg0146.shop_service.coupon.dto.request.CouponCreateRequest;
import com.lbg0146.shop_service.coupon.entity.Coupon;
import com.lbg0146.shop_service.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<Long> createCoupon(@Valid @RequestBody CouponCreateRequest request) {

        Long couponId = couponService.createCoupon(request);

        return ResponseEntity.ok(couponId);
    }

    @GetMapping("/{couponId}")
    public ResponseEntity<Coupon> findCoupon(@PathVariable Long couponId) {

        Coupon coupon = couponService.findCoupon(couponId);

        return ResponseEntity.ok(coupon);
    }

    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long couponId) {

        couponService.deleteCoupon(couponId);

        return ResponseEntity.noContent().build();
    }
}
