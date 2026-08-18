package com.lbg0146.shop_service.payment;

import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.order.entity.Order;
import com.lbg0146.shop_service.order.repository.OrderRepository;
import com.lbg0146.shop_service.payment.dto.request.PaymentCreateRequest;
import com.lbg0146.shop_service.payment.entity.Payment;
import com.lbg0146.shop_service.payment.repository.PaymentRepository;
import com.lbg0146.shop_service.payment.service.PaymentService;
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
public class PaymentServiceTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void 결제에_성공하면_결제가_생성되고_주문상태가_PAID() {

        Member member = testDataFactory.createMember();
        Order order = testDataFactory.createOrder(member);

        PaymentCreateRequest request = new PaymentCreateRequest(
                order.getId(),
                "CARD"
        );

        Long paymentId = paymentService.createPayment(request);

        Payment payment = paymentRepository.findById(paymentId).orElseThrow();

        assertThat(payment.getPaymentStatus().getCodeValue()).isEqualTo("PAID");

        assertThat(payment.getPaymentMethod().getCodeValue()).isEqualTo("CARD");

        assertThat(payment.getPaidAmount()).isEqualTo(payment.getOrder().getTotalPrice());

        assertThat(payment.getPaymentKey()).startsWith("PAY-");

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();

        assertThat(updatedOrder.getOrderStatus().getCodeValue()).isEqualTo("PAID");
    }

    @Test
    void 이미_결제된_주문은_다시_결제_불가능() {

        Member member = testDataFactory.createMember();
        Order order = testDataFactory.createOrder(member);

        PaymentCreateRequest request = new PaymentCreateRequest(
                order.getId(),
                "CARD"
        );

        paymentService.createPayment(request);

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 결제가 완료된 주문입니다.");
    }
}
