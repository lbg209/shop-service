package com.lbg0146.shop_service.delivery.service;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.code.repository.CommonCodeDetailRepository;
import com.lbg0146.shop_service.delivery.entity.Delivery;
import com.lbg0146.shop_service.delivery.repository.DeliveryRepository;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.order.entity.Order;
import com.lbg0146.shop_service.order.repository.OrderRepository;
import com.lbg0146.shop_service.order.service.OrderStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final CommonCodeDetailRepository commonCodeDetailRepository;
    private final OrderStatusHistoryService orderStatusHistoryService;

    @Transactional
    public Long createDelivery(Long orderId) {

        // 1. 주문 조회
        Order order = orderRepository.findById(orderId).orElseThrow(() ->
                        new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 2. 결제 완료 여부 확인
        if (!order.isPaid()) {
            throw new BusinessException(ErrorCode.ORDER_NOT_PAID);
        }

        // 3. 이미 배송이 생성된 주문인지 확인
        if (deliveryRepository.findByOrderId(orderId).isPresent()) {

            throw new BusinessException(ErrorCode.ALREADY_DELIVERY);
        }

        // 4. 배송 상태 조회
        CommonCodeDetail deliveryStatus = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                "DELIVERY_STATUS",
                "READY"
        ).orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_STATUS_NOT_FOUND));

        // 5. 배송 생성
        Delivery delivery = Delivery.createDelivery(order, deliveryStatus);

        deliveryRepository.save(delivery);

        return delivery.getId();
    }

    @Transactional
    public void changeDeliveryStatus(Long deliveryId, String status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_NOT_FOUND));

        CommonCodeDetail deliveryStatus = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                "DELIVERY_STATUS",
                        status
        ).orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_STATUS_NOT_FOUND));

        delivery.changeStatus(deliveryStatus);

        if ("SHIPPING".equals(status)) {

            CommonCodeDetail orderStatus = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                            "ORDER_STATUS",
                            "SHIPPING"
            ).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_STATUS_NOT_FOUND));

            delivery.startShipping();
            orderStatusHistoryService.changeStatus(delivery.getOrder(), orderStatus, null);
        }

        if ("DELIVERED".equals(status)) {

            CommonCodeDetail orderStatus = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                            "ORDER_STATUS",
                            "DELIVERED"
            ).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_STATUS_NOT_FOUND));

            delivery.completeDelivery();
            orderStatusHistoryService.changeStatus(delivery.getOrder(), orderStatus, null);
        }
    }
}
