package com.lbg0146.shop_service.order;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.code.repository.CommonCodeDetailRepository;
import com.lbg0146.shop_service.delivery.entity.Delivery;
import com.lbg0146.shop_service.delivery.service.DeliveryService;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.order.entity.Order;
import com.lbg0146.shop_service.order.entity.OrderStatusHistory;
import com.lbg0146.shop_service.order.repository.OrderStatusHistoryRepository;
import com.lbg0146.shop_service.order.service.OrderStatusHistoryService;
import com.lbg0146.shop_service.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class OrderStatusHistoryServiceTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private OrderStatusHistoryService orderStatusHistoryService;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private CommonCodeDetailRepository commonCodeDetailRepository;

    @Test
    void 주문_생성시_CREATE_이력이_저장() {

        Member member = testDataFactory.createMember();
        Order order = testDataFactory.createOrder(member);

        List<OrderStatusHistory> histories = orderStatusHistoryRepository.findAllByOrderId(order.getId());

        assertThat(histories).hasSize(1);

        OrderStatusHistory history = histories.get(0);

        assertThat(history.getStatus().getCodeValue()).isEqualTo("ORDERED");

        assertThat(history.getChangeType().getCodeValue()).isEqualTo("CREATE");
    }

    @Test
    void 주문_상태를_변경하면_UPDATE_이력이_저장() {

        Member member = testDataFactory.createMember();
        Order order = testDataFactory.createOrder(member);

        CommonCodeDetail paidStatus = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                "ORDER_STATUS",
                "PAID"
        ).orElseThrow();

        orderStatusHistoryService.changeStatus(order, paidStatus, null);

        List<OrderStatusHistory> histories = orderStatusHistoryRepository.findAllByOrderId(order.getId());

        assertThat(histories).hasSize(2);

        OrderStatusHistory history = histories.get(1);

        assertThat(history.getOrder().getId()).isEqualTo(order.getId());

        assertThat(history.getStatus().getCodeValue()).isEqualTo("PAID");

        assertThat(history.getChangeType().getCodeValue()).isEqualTo("UPDATE");

        assertThat(history.getChangedBy()).isNull();

        assertThat(order.getOrderStatus().getCodeValue()).isEqualTo("PAID");
    }


}
