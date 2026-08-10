package com.lbg0146.shop_service.delivery.controller;

import com.lbg0146.shop_service.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/{orderId}")
    public ResponseEntity<Long> createDelivery(@PathVariable Long orderId) {

        Long deliveryId = deliveryService.createDelivery(orderId);

        return ResponseEntity.ok(deliveryId);
    }

    @PatchMapping("/{deliveryId}/status")
    public ResponseEntity<Void> changeDeliveryStatus(@PathVariable Long deliveryId, @RequestParam String status) {

        deliveryService.changeDeliveryStatus(deliveryId, status);

        return ResponseEntity.ok().build();
    }
}
