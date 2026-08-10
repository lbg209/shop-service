package com.lbg0146.shop_service.coupon;

import com.lbg0146.shop_service.coupon.dto.request.CouponCreateRequest;
import com.lbg0146.shop_service.coupon.entity.Coupon;
import com.lbg0146.shop_service.coupon.repository.CouponRepository;
import com.lbg0146.shop_service.coupon.service.CouponService;
import com.lbg0146.shop_service.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void 정률_쿠폰_생성_성공() {

        CouponCreateRequest request = new CouponCreateRequest(
                "신규회원 10% 할인",
                "RATE",
                10,
                30000L,
                10000L,
                30
        );

        Long couponId = couponService.createCoupon(request);

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();

        assertThat(coupon.getCouponName()).isEqualTo("신규회원 10% 할인");

        assertThat(coupon.getDiscountType().getCodeValue()).isEqualTo("RATE");

        assertThat(coupon.getDiscountValue()).isEqualTo(10);

        assertThat(coupon.getMinOrderAmount()).isEqualTo(30000L);

        assertThat(coupon.getMaxDiscountAmount()).isEqualTo(10000L);

        assertThat(coupon.getValidDays()).isEqualTo(30);
    }

    @Test
    void 정액_쿠폰_생성_성공() {

        CouponCreateRequest request = new CouponCreateRequest(
                "신규회원 5000원 할인",
                "AMOUNT",
                5000,
                30000L,
                null,
                30
        );

        Long couponId = couponService.createCoupon(request);

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();

        assertThat(coupon.getCouponName()).isEqualTo("신규회원 5000원 할인");

        assertThat(coupon.getDiscountType().getCodeValue()).isEqualTo("AMOUNT");

        assertThat(coupon.getDiscountValue()).isEqualTo(5000);

        assertThat(coupon.getMaxDiscountAmount()).isNull();
    }

    @Test
    void 존재하지_않는_할인방식이면_쿠폰생성_실패() {

        CouponCreateRequest request = new CouponCreateRequest(
                "잘못된 쿠폰",
                "INVALID",
                10,
                30000L,
                10000L,
                30
        );

        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("유효하지 않은 할인 방식입니다.");
    }

    @Test
    void 정률_할인율이_100을_초과하면_실패() {

        CouponCreateRequest request = new CouponCreateRequest(
                "잘못된 쿠폰",
                "RATE",
                101,
                30000L,
                10000L,
                30
        );

        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("할인 값이 올바르지 않습니다.");
    }

    @Test
    void 할인값이_0이면_실패() {

        CouponCreateRequest request = new CouponCreateRequest(
                "잘못된 쿠폰",
                "RATE",
                0,
                30000L,
                10000L,
                30
        );

        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("할인 값이 올바르지 않습니다.");
    }

    @Test
    void 최소주문금액이_음수면_실패() {

        CouponCreateRequest request = new CouponCreateRequest(
                "잘못된 쿠폰",
                "RATE",
                10,
                -1L,
                10000L,
                30
        );

        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("최소 주문 금액이 올바르지 않습니다.");
    }

    @Test
    void 사용기간이_0이면_실패() {

        CouponCreateRequest request = new CouponCreateRequest(
                "잘못된 쿠폰",
                "RATE",
                10,
                30000L,
                10000L,
                0
        );

        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("쿠폰 사용 가능 기간이 올바르지 않습니다.");
    }

    @Test
    void 정액할인에_최대할인금액을_설정하면_실패() {

        CouponCreateRequest request = new CouponCreateRequest(
                "잘못된 쿠폰",
                "AMOUNT",
                5000,
                30000L,
                10000L,
                30
        );

        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("최대 할인 금액이 올바르지 않습니다.");
    }

    @Test
    void 쿠폰_조회_성공() {

        CouponCreateRequest request = new CouponCreateRequest(
                "신규회원 할인",
                "RATE",
                10,
                30000L,
                10000L,
                30
        );

        Long couponId = couponService.createCoupon(request);

        Coupon coupon = couponService.findCoupon(couponId);

        assertThat(coupon.getId()).isEqualTo(couponId);

        assertThat(coupon.getCouponName()).isEqualTo("신규회원 할인");
    }

    @Test
    void 쿠폰_삭제_성공() {

        CouponCreateRequest request = new CouponCreateRequest(
                "삭제할 쿠폰",
                "RATE",
                10,
                30000L,
                10000L,
                30
        );

        Long couponId = couponService.createCoupon(request);

        couponService.deleteCoupon(couponId);

        assertThatThrownBy(() -> couponService.findCoupon(couponId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("쿠폰을 찾을 수 없습니다.");
    }

    @Test
    void 존재하지_않는_쿠폰_조회_실패() {

        assertThatThrownBy(() -> couponService.findCoupon(999999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("쿠폰을 찾을 수 없습니다.");
    }
}
