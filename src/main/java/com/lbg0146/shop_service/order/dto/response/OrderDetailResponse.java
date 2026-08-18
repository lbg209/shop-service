package com.lbg0146.shop_service.order.dto.response;

import com.lbg0146.shop_service.delivery.dto.response.DeliveryResponse;
import com.lbg0146.shop_service.order.entity.Order;
import com.lbg0146.shop_service.order.entity.OrderItem;
import com.lbg0146.shop_service.payment.dto.response.PaymentResponse;

import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        String orderNumber,
        List<OrderItemResponse> items,
        Long totalPrice,
        Long gradeDiscountAmount,
        Long couponDiscountAmount,
        Long discountAmount,
        Long finalPrice,
        DeliveryResponse delivery,
        PaymentResponse payment
) {

    public static OrderDetailResponse from(
            Order order,
            List<OrderItem> orderItems
    ) {
        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                orderItems.stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                order.getTotalPrice(),
                order.getGradeDiscountAmount(),
                order.getCouponDiscountAmount(),
                order.getDiscountAmount(),
                order.getFinalPrice(),
                DeliveryResponse.from(order.getDelivery()),
                PaymentResponse.from(order.getPayment())
        );
    }
}