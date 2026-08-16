package com.lbg0146.shop_service.batch.dto;

public record MemberPurchaseSumDto(
        Long memberId,
        Long totalPurchaseAmount
) {
}