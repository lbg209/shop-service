package com.lbg0146.shop_service.cart.dto.response;

import java.util.List;

public record CartResponse (
        Long cartId,
        List<CartItemResponse> items
){
}
