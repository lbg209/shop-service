package com.lbg0146.shop_service.payment.controller;

import com.lbg0146.shop_service.payment.dto.request.PaymentCreateRequest;
import com.lbg0146.shop_service.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Long> createPayment(@Valid @RequestBody PaymentCreateRequest request) {

        Long paymentId = paymentService.createPayment(request);

        return ResponseEntity.ok(paymentId);
    }
}
