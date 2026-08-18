package com.lbg0146.shop_service.delivery;

import com.lbg0146.shop_service.delivery.service.DeliveryService;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.order.entity.Order;
import com.lbg0146.shop_service.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private DeliveryService deliveryService;

    @Test
    void 배송을_생성하면_성공() throws Exception {

        Member member = testDataFactory.createMember();
        Order order = testDataFactory.createPaidOrder(member);

        mockMvc.perform(
                        post("/api/deliveries/{orderId}", order.getId())
                )
                .andExpect(status().isOk());
    }

    @Test
    void 배송상태를_SHIPPING으로_변경하면_성공() throws Exception {

        Member member = testDataFactory.createMember();
        Order order = testDataFactory.createPaidOrder(member);

        Long deliveryId = deliveryService.createDelivery(order.getId());

        mockMvc.perform(
                        patch("/api/deliveries/{deliveryId}/status", deliveryId)
                                .param("status", "SHIPPING")
                )
                .andExpect(status().isOk());
    }
}
