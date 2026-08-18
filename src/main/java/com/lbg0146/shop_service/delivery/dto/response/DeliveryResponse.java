package com.lbg0146.shop_service.delivery.dto.response;

import com.lbg0146.shop_service.delivery.entity.Delivery;

import java.time.LocalDateTime;

public record DeliveryResponse(
        Long deliveryId,
        String deliveryStatus,
        String trackingNumber,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt
) {

    public static DeliveryResponse from(Delivery delivery) {
        if (delivery == null) {
            return null;
        }

        return new DeliveryResponse(
                delivery.getId(),
                delivery.getDeliveryStatus().getCodeValue(),
                delivery.getTrackingNumber(),
                delivery.getShippedAt(),
                delivery.getDeliveredAt()
        );
    }
}