package com.lbg0146.shop_service.coupon.service;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.code.repository.CommonCodeDetailRepository;
import com.lbg0146.shop_service.coupon.dto.request.CouponCreateRequest;
import com.lbg0146.shop_service.coupon.entity.Coupon;
import com.lbg0146.shop_service.coupon.repository.CouponRepository;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final CommonCodeDetailRepository commonCodeDetailRepository;

    @Transactional
    public Long createCoupon(CouponCreateRequest request) {

        // 할인 방식 조회
        CommonCodeDetail discountType = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                "DISCOUNT_TYPE",
                        request.discountType()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_DISCOUNT_TYPE));

        // 할인 정책 검증
        validateDiscount(request.discountType(), request.discountValue(), request.maxDiscountAmount());

        // 최소 주문 금액 검증
        if (request.minOrderAmount() < 0) {

            throw new BusinessException(ErrorCode.INVALID_MIN_ORDER_AMOUNT);
        }

        // 사용 가능 기간 검증
        if (request.validDays() <= 0) {

            throw new BusinessException(ErrorCode.INVALID_VALID_DAYS);
        }

        Coupon coupon = Coupon.createCoupon(
                discountType,
                request.couponName(),
                request.discountValue(),
                request.minOrderAmount(),
                request.maxDiscountAmount(),
                request.validDays()
        );

        Coupon savedCoupon = couponRepository.save(coupon);

        return savedCoupon.getId();
    }

    private void validateDiscount(String discountType, Integer discountValue, Long maxDiscountAmount) {

        // 할인 값 기본 검증
        if (discountValue == null || discountValue <= 0) {

            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_VALUE);
        }

        // 정률 할인
        if ("RATE".equals(discountType)) {

            // 할인율은 1 ~ 100
            if (discountValue > 100) {

                throw new BusinessException(ErrorCode.INVALID_DISCOUNT_VALUE);
            }

            // 최대 할인 금액이 있다면 0보다 커야 함
            if (maxDiscountAmount != null && maxDiscountAmount <= 0) {

                throw new BusinessException(ErrorCode.INVALID_MAX_DISCOUNT_AMOUNT);
            }

            return;
        }

        // 정액 할인
        if ("AMOUNT".equals(discountType)) {
            // 정액 할인에는 최대 할인 금액이 필요 없음
            if (maxDiscountAmount != null) {

                throw new BusinessException(ErrorCode.INVALID_MAX_DISCOUNT_AMOUNT);
            }

            return;
        }

        throw new BusinessException(ErrorCode.INVALID_DISCOUNT_TYPE);
    }

    public Coupon findCoupon(Long couponId) {

        return couponRepository.findByIdAndDeletedAtIsNull(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
    }

    @Transactional
    public void deleteCoupon(Long couponId) {

        Coupon coupon = couponRepository.findByIdAndDeletedAtIsNull(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        coupon.delete();
    }
}
