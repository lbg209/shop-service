package com.lbg0146.shop_service.order;

import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.order.dto.request.OrderCreateRequest;
import com.lbg0146.shop_service.order.dto.request.OrderItemRequest;
import com.lbg0146.shop_service.order.entity.Order;
import com.lbg0146.shop_service.order.entity.OrderItem;
import com.lbg0146.shop_service.order.repository.OrderItemRepository;
import com.lbg0146.shop_service.order.repository.OrderRepository;
import com.lbg0146.shop_service.order.service.OrderService;
import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class OrderServiceTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void 단건_주문이_정상적으로_생성() {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();

        int beforeStock = product.getStockQuantity();

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(
                        new OrderItemRequest(product.getId(), 2)
                ),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호"
        );

        Long orderId = orderService.createOrder(
                member.getId(),
                request
        );

        Order order = orderRepository.findById(orderId).orElseThrow();

        assertThat(order.getMember().getId()).isEqualTo(member.getId());

        assertThat(order.getTotalPrice()).isEqualTo(product.getPrice() * 2);

        List<OrderItem> orderItems = orderItemRepository.findAll();

        assertThat(orderItems).hasSize(1);

        assertThat(orderItems.get(0).getProduct().getId()).isEqualTo(product.getId());

        assertThat(orderItems.get(0).getQuantity()).isEqualTo(2);

        assertThat(product.getStockQuantity()).isEqualTo(beforeStock - 2);
    }

    @Test
    void 재고보다_많이_주문하면_실패() {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();

        int stock = product.getStockQuantity();

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(
                        new OrderItemRequest(
                                product.getId(),
                                stock + 1
                        )
                ),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호"
        );

        assertThatThrownBy(() -> orderService.createOrder(member.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("재고가 부족합니다.");
    }
}
