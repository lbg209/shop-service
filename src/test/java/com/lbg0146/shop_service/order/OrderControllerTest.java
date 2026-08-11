package com.lbg0146.shop_service.order;

import com.lbg0146.shop_service.cart.entity.Cart;
import com.lbg0146.shop_service.coupon.entity.Coupon;
import com.lbg0146.shop_service.coupon.entity.MemberCoupon;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.order.dto.request.OrderCreateRequest;
import com.lbg0146.shop_service.order.dto.request.OrderItemRequest;
import com.lbg0146.shop_service.product.entity.Product;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class OrderControllerTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 단건_주문_성공() throws Exception {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();

        OrderItemRequest item = new OrderItemRequest(
                product.getId(),
                1
        );

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(item),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                null
        );

        mockMvc.perform(
                        post("/api/orders/direct")
                                .param("memberId", member.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }


    @Test
    void 재고보다_많이_주문하면_실패() throws Exception {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();

        OrderItemRequest item = new OrderItemRequest(
                product.getId(),
                product.getStockQuantity() + 1
        );

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(item),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                null
        );

        mockMvc.perform(
                        post("/api/orders/direct")
                                .param("memberId", member.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void 장바구니_주문_성공() throws Exception {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();

        Cart cart = testDataFactory.createCart(member);

        testDataFactory.createCartItem(cart, product, 2);

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                null
        );

        mockMvc.perform(
                        post("/api/orders/cart")
                                .param(
                                        "memberId",
                                        member.getId().toString()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void 쿠폰을_적용해서_단건_주문_성공() throws Exception {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();
        Coupon coupon = testDataFactory.createRateCoupon();

        MemberCoupon memberCoupon = testDataFactory.createMemberCoupon(member, coupon);

        OrderItemRequest item = new OrderItemRequest(product.getId(), 1);

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(item),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                memberCoupon.getId()
        );

        mockMvc.perform(
                        post("/api/orders/direct")
                                .param("memberId", member.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk());
    }
}
