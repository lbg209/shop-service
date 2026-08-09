package com.lbg0146.shop_service.order.controller;

import com.lbg0146.shop_service.order.dto.request.OrderCreateRequest;
import com.lbg0146.shop_service.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/direct")
    public ResponseEntity<Long> directOrder(@RequestParam Long memberId, @Valid @RequestBody OrderCreateRequest request) {

        Long orderId = orderService.createOrder(memberId, request);

        return ResponseEntity.ok(orderId);
    }

    @PostMapping("/cart")
    public ResponseEntity<Long> cartOrder(@RequestParam Long memberId, @Valid @RequestBody OrderCreateRequest request) {

        Long orderId = orderService.createCartOrder(memberId, request);

        return ResponseEntity.ok(orderId);
    }

}
