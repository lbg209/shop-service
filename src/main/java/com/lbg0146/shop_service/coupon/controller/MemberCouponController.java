package com.lbg0146.shop_service.coupon.controller;

import com.lbg0146.shop_service.coupon.dto.request.MemberCouponIssueRequest;
import com.lbg0146.shop_service.coupon.service.MemberCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member-coupons")
public class MemberCouponController {

    private final MemberCouponService memberCouponService;

    @PostMapping
    public ResponseEntity<Long> issueCoupon(@RequestBody MemberCouponIssueRequest request) {

        Long memberCouponId = memberCouponService.issueCoupon(request);

        return ResponseEntity.ok(memberCouponId);
    }

    @PatchMapping("/{memberCouponId}/use")
    public ResponseEntity<Void> useCoupon(@PathVariable Long memberCouponId, @RequestParam Long memberId) {

        memberCouponService.useCoupon(memberId, memberCouponId);

        return ResponseEntity.ok().build();
    }
}
