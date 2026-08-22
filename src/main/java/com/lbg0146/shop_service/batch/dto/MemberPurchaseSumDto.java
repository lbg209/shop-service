package com.lbg0146.shop_service.batch.dto;

import com.lbg0146.shop_service.member.entity.Member;

public record MemberPurchaseSumDto(
        Member member,
        Long totalPurchaseAmount
) {
}