package com.lbg0146.shop_service.coupon;

import com.lbg0146.shop_service.coupon.dto.request.CouponCreateRequest;
import com.lbg0146.shop_service.coupon.entity.Coupon;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void 쿠폰을_생성하면_성공() throws Exception {

        CouponCreateRequest request = new CouponCreateRequest(
                "신규회원 할인쿠폰",
                "RATE",
                10,
                10000L,
                5000L,
                30
        );

        mockMvc.perform(
                        post("/api/coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void 쿠폰을_조회하면_성공() throws Exception {

        Coupon coupon = testDataFactory.createRateCoupon();

        mockMvc.perform(
                        get("/api/coupons/{couponId}", coupon.getId())
                )
                .andExpect(status().isOk());
    }

    @Test
    void 쿠폰을_삭제하면_성공() throws Exception {

        Coupon coupon = testDataFactory.createRateCoupon();

        mockMvc.perform(
                        delete("/api/coupons/{couponId}", coupon.getId())
                )
                .andExpect(status().isNoContent());
    }
}
