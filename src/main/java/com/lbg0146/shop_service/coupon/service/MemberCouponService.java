package com.lbg0146.shop_service.coupon.service;

import com.lbg0146.shop_service.coupon.dto.request.MemberCouponIssueRequest;
import com.lbg0146.shop_service.coupon.entity.Coupon;
import com.lbg0146.shop_service.coupon.entity.MemberCoupon;
import com.lbg0146.shop_service.coupon.repository.CouponRepository;
import com.lbg0146.shop_service.coupon.repository.MemberCouponRepository;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCouponService {

    private final MemberCouponRepository memberCouponRepository;
    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public Long issueCoupon(MemberCouponIssueRequest request) {

        Member member = memberRepository.findByIdAndDeletedAtIsNull(request.memberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Coupon coupon = couponRepository.findByIdAndDeletedAtIsNull(request.couponId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        if (memberCouponRepository.existsByMemberIdAndCouponId(request.memberId(), request.couponId())) {
            throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        MemberCoupon memberCoupon = MemberCoupon.createMemberCoupon(member, coupon);

        MemberCoupon savedMemberCoupon = memberCouponRepository.save(memberCoupon);

        return savedMemberCoupon.getId();
    }

    @Transactional
    public void useCoupon(Long memberId, Long memberCouponId) {

        MemberCoupon memberCoupon = memberCouponRepository.findByIdAndMemberId(memberCouponId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_COUPON_NOT_FOUND));

        memberCoupon.use();
    }

}
