package com.lbg0146.shop_service.delivery;

import com.lbg0146.shop_service.delivery.entity.Delivery;
import com.lbg0146.shop_service.delivery.repository.DeliveryRepository;
import com.lbg0146.shop_service.delivery.service.DeliveryService;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.order.entity.Order;
import com.lbg0146.shop_service.order.entity.OrderStatusHistory;
import com.lbg0146.shop_service.order.repository.OrderStatusHistoryRepository;
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
public class DeliveryServiceTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Test
    void 배송을_생성하면_READY_상태() {

        Order order = testDataFactory.createPaidOrder();

        Long deliveryId = deliveryService.createDelivery(order.getId());

        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();

        assertThat(delivery.getOrder().getId()).isEqualTo(order.getId());

        assertThat(delivery.getDeliveryStatus().getCodeValue()).isEqualTo("READY");
    }

    @Test
    void 배송을_SHIPPING으로_변경하면_주문도_SHIPPING으로_변경() {

        Order order = testDataFactory.createPaidOrder();

        Long deliveryId = deliveryService.createDelivery(order.getId());

        deliveryService.changeDeliveryStatus(deliveryId, "SHIPPING");

        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();

        assertThat(delivery.getDeliveryStatus().getCodeValue()).isEqualTo("SHIPPING");

        assertThat(delivery.getShippedAt()).isNotNull();

        assertThat(order.getOrderStatus().getCodeValue()).isEqualTo("SHIPPING");
    }

    @Test
    void 이미_배송이_생성된_주문은_배송을_생성_불가능() {

        Order order = testDataFactory.createPaidOrder();

        // 첫 번째 배송 생성
        deliveryService.createDelivery(order.getId());

        assertThatThrownBy(() -> deliveryService.createDelivery(order.getId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.ALREADY_DELIVERY
                );
    }

    @Test
    void 배송을_SHIPPING으로_변경하면_주문상태와_이력이_함께_변경() {

        // given
        Order order = testDataFactory.createPaidOrder();

        Long deliveryId = deliveryService.createDelivery(order.getId());

        // when
        deliveryService.changeDeliveryStatus(deliveryId, "SHIPPING");

        // then
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();

        assertThat(delivery.getDeliveryStatus().getCodeValue()).isEqualTo("SHIPPING");

        assertThat(delivery.getShippedAt()).isNotNull();

        // 주문 상태 확인
        assertThat(order.getOrderStatus().getCodeValue()).isEqualTo("SHIPPING");

        // 주문 상태 이력 확인
        List<OrderStatusHistory> histories = orderStatusHistoryRepository.findAllByOrderId(order.getId());

        OrderStatusHistory history = histories.get(histories.size() - 1);

        assertThat(history.getStatus().getCodeValue()).isEqualTo("SHIPPING");

        assertThat(history.getChangeType().getCodeValue()).isEqualTo("UPDATE");

        assertThat(history.getChangedBy()).isNull();
    }

    @Test
    void 배송을_DELIVERED로_변경하면_주문상태와_이력이_함께_변경() {

        Order order = testDataFactory.createPaidOrder();

        Long deliveryId = deliveryService.createDelivery(order.getId());

        deliveryService.changeDeliveryStatus(deliveryId, "SHIPPING");
        deliveryService.changeDeliveryStatus(deliveryId, "DELIVERED");

        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();

        assertThat(delivery.getDeliveryStatus().getCodeValue()).isEqualTo("DELIVERED");

        assertThat(delivery.getDeliveredAt()).isNotNull();

        assertThat(order.getOrderStatus().getCodeValue()).isEqualTo("DELIVERED");

        List<OrderStatusHistory> histories = orderStatusHistoryRepository.findAllByOrderId(order.getId());

        OrderStatusHistory history = histories.get(histories.size() - 1);

        assertThat(history.getStatus().getCodeValue()).isEqualTo("DELIVERED");

        assertThat(history.getChangeType().getCodeValue()).isEqualTo("UPDATE");
    }

}
