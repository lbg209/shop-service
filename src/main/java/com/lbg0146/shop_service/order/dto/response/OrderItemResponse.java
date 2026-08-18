package com.lbg0146.shop_service.order.dto.response;

import com.lbg0146.shop_service.order.entity.OrderItem;

public record OrderItemResponse(
        Long orderItemId,
        Long productId,
        String productName,
        Long orderPrice,
        Integer quantity
) {

    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProductName(),
                orderItem.getOrderPrice(),
                orderItem.getQuantity()
        );
    }
}