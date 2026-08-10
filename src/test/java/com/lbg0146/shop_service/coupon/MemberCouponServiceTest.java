package com.lbg0146.shop_service.coupon;

import com.lbg0146.shop_service.coupon.dto.request.MemberCouponIssueRequest;
import com.lbg0146.shop_service.coupon.entity.Coupon;
import com.lbg0146.shop_service.coupon.entity.CouponStatus;
import com.lbg0146.shop_service.coupon.entity.MemberCoupon;
import com.lbg0146.shop_service.coupon.repository.MemberCouponRepository;
import com.lbg0146.shop_service.coupon.service.MemberCouponService;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.support.TestDataFactory;
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
public class MemberCouponServiceTest {

    @Autowired
    private MemberCouponService memberCouponService;

    @Autowired
    private MemberCouponRepository memberCouponRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void 쿠폰_발급_성공() {

        Member member = testDataFactory.createMember();
        Coupon coupon = testDataFactory.createRateCoupon();

        MemberCouponIssueRequest request = new MemberCouponIssueRequest(member.getId(), coupon.getId());

        Long memberCouponId = memberCouponService.issueCoupon(request);

        MemberCoupon memberCoupon = memberCouponRepository.findById(memberCouponId).orElseThrow();

        assertThat(memberCoupon.getMember().getId()).isEqualTo(member.getId());

        assertThat(memberCoupon.getCoupon().getId()).isEqualTo(coupon.getId());

        assertThat(memberCoupon.getStatus()).isEqualTo(CouponStatus.ISSUED);

        assertThat(memberCoupon.getIssuedAt()).isNotNull();

        assertThat(memberCoupon.getExpiredAt()).isNotNull();
    }

    @Test
    void 이미_발급된_쿠폰은_중복_발급_불가능() {

        Member member = testDataFactory.createMember();
        Coupon coupon = testDataFactory.createRateCoupon();

        MemberCouponIssueRequest request = new MemberCouponIssueRequest(member.getId(), coupon.getId());

        memberCouponService.issueCoupon(request);

        assertThatThrownBy(() -> memberCouponService.issueCoupon(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 발급받은 쿠폰입니다.");
    }

    @Test
    void 존재하지_않는_회원에게_쿠폰_발급_실패() {

        Coupon coupon = testDataFactory.createRateCoupon();

        MemberCouponIssueRequest request = new MemberCouponIssueRequest(999999L, coupon.getId());

        assertThatThrownBy(() -> memberCouponService.issueCoupon(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("회원을 찾을 수 없습니다.");
    }
    @Test
    void 존재하지_않는_쿠폰_발급_실패() {

        Member member = testDataFactory.createMember();

        MemberCouponIssueRequest request = new MemberCouponIssueRequest(member.getId(), 999999L);

        assertThatThrownBy(() -> memberCouponService.issueCoupon(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("쿠폰을 찾을 수 없습니다.");
    }

    @Test
    void 쿠폰_사용_성공() {

        Member member = testDataFactory.createMember();
        Coupon coupon = testDataFactory.createRateCoupon();

        Long memberCouponId = memberCouponService.issueCoupon(
                new MemberCouponIssueRequest(member.getId(), coupon.getId()));

        memberCouponService.useCoupon(member.getId(), memberCouponId);

        MemberCoupon memberCoupon = memberCouponRepository.findById(memberCouponId).orElseThrow();

        assertThat(memberCoupon.getStatus()).isEqualTo(CouponStatus.USED);

        assertThat(memberCoupon.getUsedAt()).isNotNull();
    }

    @Test
    void 이미_사용한_쿠폰은_다시_사용할_수_없음() {

        Member member = testDataFactory.createMember();
        Coupon coupon = testDataFactory.createRateCoupon();

        Long memberCouponId = memberCouponService.issueCoupon(
                new MemberCouponIssueRequest(member.getId(), coupon.getId()));

        memberCouponService.useCoupon(member.getId(), memberCouponId);

        assertThatThrownBy(() -> memberCouponService.useCoupon(member.getId(), memberCouponId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("사용할 수 없는 쿠폰입니다.");
    }

    @Test
    void 존재하지_않는_회원쿠폰은_사용할_수_없음() {

        Member member = testDataFactory.createMember();

        assertThatThrownBy(() -> memberCouponService.useCoupon(member.getId(), 999999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("회원 쿠폰을 찾을 수 없습니다.");
    }

    @Test
    void 만료된_쿠폰은_사용할_수_없음() {

        Member member = testDataFactory.createMember();
        Coupon coupon = testDataFactory.createRateCoupon();

        Long memberCouponId = memberCouponService.issueCoupon(
                new MemberCouponIssueRequest(member.getId(), coupon.getId()));

        MemberCoupon memberCoupon = memberCouponRepository.findById(memberCouponId).orElseThrow();

        memberCoupon.expire();

        assertThatThrownBy(() -> memberCouponService.useCoupon(member.getId(), memberCouponId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("쿠폰이 만료되었습니다.");

        assertThat(memberCoupon.getStatus()).isEqualTo(CouponStatus.EXPIRED);
    }

}
