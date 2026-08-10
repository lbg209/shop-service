package com.lbg0146.shop_service.payment.service;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.code.repository.CommonCodeDetailRepository;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.order.entity.Order;
import com.lbg0146.shop_service.order.repository.OrderRepository;
import com.lbg0146.shop_service.payment.dto.request.PaymentCreateRequest;
import com.lbg0146.shop_service.payment.entity.Payment;
import com.lbg0146.shop_service.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CommonCodeDetailRepository commonCodeDetailRepository;

    @Transactional
    public Long createPayment(PaymentCreateRequest request) {

        // 1. 주문 조회
        Order order = orderRepository.findById(request.orderId()).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 2. 이미 결제된 주문인지 확인
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {

            throw new BusinessException(ErrorCode.ALREADY_PAID);
        }

        // 3. 결제 수단 조회
        CommonCodeDetail paymentMethod = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                "PAYMENT_METHOD",
                        request.paymentMethod())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

        // 4. 결제 상태 조회
        CommonCodeDetail paymentStatus = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                        "PAYMENT_STATUS",
                        "PAID"
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_STATUS_NOT_FOUND));

        // 5. 주문 상태 조회
        CommonCodeDetail orderStatus = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                        "ORDER_STATUS",
                        "PAID"
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_STATUS_NOT_FOUND));

        // 6. 가상 결제 승인 키 생성
        String paymentKey = "PAY-" + UUID.randomUUID();

        // 7. 결제 생성
        Payment payment = Payment.createPayment(
                order,
                paymentStatus,
                paymentMethod,
                order.getFinalPrice(),
                paymentKey,
                LocalDateTime.now()
        );

        paymentRepository.save(payment);

        // 8. 주문 상태를 결제완료로 변경
        order.changeStatus(orderStatus);

        return payment.getId();
    }
}
