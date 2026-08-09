package com.lbg0146.shop_service.cart.controller;

import com.lbg0146.shop_service.cart.dto.request.CartItemCreateRequest;
import com.lbg0146.shop_service.cart.dto.request.CartItemUpdateRequest;
import com.lbg0146.shop_service.cart.dto.response.CartResponse;
import com.lbg0146.shop_service.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    // 장바구니 조회
    @GetMapping
    public CartResponse getCart(@RequestParam Long memberId) {
        return cartService.getCart(memberId);
    }

    // 장바구니 상품 추가
    @PostMapping("/items")
    public ResponseEntity<Void> addItem(@RequestParam Long memberId, @Valid @RequestBody CartItemCreateRequest request) {
        cartService.addItem(memberId, request);

        return ResponseEntity.ok().build();
    }

    // 장바구니 상품 수량 변경
    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<Void> updateItem(@RequestParam Long memberId, @PathVariable Long cartItemId, @Valid @RequestBody CartItemUpdateRequest request) {
        cartService.updateItem(
                memberId,
                cartItemId,
                request
        );

        return ResponseEntity.ok().build();
    }

    // 장바구니 상품 삭제
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> deleteItem(
            @RequestParam Long memberId,
            @PathVariable Long cartItemId
    ) {
        cartService.deleteItem(
                memberId,
                cartItemId
        );

        return ResponseEntity.noContent().build();
    }
}
