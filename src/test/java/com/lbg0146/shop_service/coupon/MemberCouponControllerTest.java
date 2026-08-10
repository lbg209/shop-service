package com.lbg0146.shop_service.coupon;

import com.lbg0146.shop_service.coupon.dto.request.MemberCouponIssueRequest;
import com.lbg0146.shop_service.coupon.entity.Coupon;
import com.lbg0146.shop_service.coupon.service.MemberCouponService;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class MemberCouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private MemberCouponService memberCouponService;

    @Test
    void 회원에게_쿠폰을_발급하면_성공() throws Exception {

        Member member = testDataFactory.createMember();
        Coupon coupon = testDataFactory.createRateCoupon();

        MemberCouponIssueRequest request = new MemberCouponIssueRequest(member.getId(), coupon.getId());

        mockMvc.perform(
                        post("/api/member-coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void 존재하지_않는_회원에게_쿠폰_발급하면_실패() throws Exception {

        Coupon coupon = testDataFactory.createRateCoupon();

        MemberCouponIssueRequest request = new MemberCouponIssueRequest(999999L, coupon.getId());

        mockMvc.perform(
                        post("/api/member-coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void 존재하지_않는_쿠폰을_발급하면_실패() throws Exception {

        Member member = testDataFactory.createMember();

        MemberCouponIssueRequest request = new MemberCouponIssueRequest(member.getId(), 999999L);

        mockMvc.perform(
                        post("/api/member-coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void 이미_발급된_쿠폰을_다시_발급하면_실패() throws Exception {

        Member member = testDataFactory.createMember();
        Coupon coupon = testDataFactory.createRateCoupon();

        MemberCouponIssueRequest request = new MemberCouponIssueRequest(member.getId(), coupon.getId());

        // 첫 번째 발급
        mockMvc.perform(
                        post("/api/member-coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        // 두 번째 발급
        mockMvc.perform(
                        post("/api/member-coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void 회원쿠폰을_사용하면_성공() throws Exception {

        Member member = testDataFactory.createMember();
        Coupon coupon = testDataFactory.createRateCoupon();

        Long memberCouponId = memberCouponService.issueCoupon(
                new MemberCouponIssueRequest(member.getId(), coupon.getId()));

        mockMvc.perform(
                        patch(
                                "/api/member-coupons/{memberCouponId}/use",
                                memberCouponId
                        )
                                .param(
                                        "memberId",
                                        member.getId().toString()
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void 존재하지_않는_회원쿠폰을_사용하면_실패() throws Exception {

        Member member = testDataFactory.createMember();

        mockMvc.perform(
                        patch(
                                "/api/member-coupons/{memberCouponId}/use",
                                999999L
                        )
                                .param(
                                        "memberId",
                                        member.getId().toString()
                                )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void 이미_사용한_회원쿠폰을_다시_사용하면_실패() throws Exception {

        Member member = testDataFactory.createMember();
        Coupon coupon = testDataFactory.createRateCoupon();

        Long memberCouponId = memberCouponService.issueCoupon(
                new MemberCouponIssueRequest(member.getId(), coupon.getId()));

        // 첫 번째 사용
        memberCouponService.useCoupon(member.getId(), memberCouponId);

        // 두 번째 사용
        mockMvc.perform(
                        patch(
                                "/api/member-coupons/{memberCouponId}/use",
                                memberCouponId
                        )
                                .param(
                                        "memberId",
                                        member.getId().toString()
                                )
                )
                .andExpect(status().isBadRequest());
    }
}
