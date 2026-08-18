package com.lbg0146.shop_service.order.dto.response;

import com.lbg0146.shop_service.order.entity.Order;
import com.lbg0146.shop_service.order.entity.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String orderNumber,
        Long totalPrice,
        Long gradeDiscountAmount,
        Long couponDiscountAmount,
        Long discountAmount,
        Long finalPrice,
        LocalDateTime orderedAt,
        List<OrderItemResponse> items
) {

    public static OrderResponse from(
            Order order,
            List<OrderItem> orderItems
    ) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getGradeDiscountAmount(),
                order.getCouponDiscountAmount(),
                order.getDiscountAmount(),
                order.getFinalPrice(),
                order.getOrderedAt(),
                orderItems.stream()
                        .map(OrderItemResponse::from)
                        .toList()
        );
    }
}