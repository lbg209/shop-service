package com.lbg0146.shop_service.member.dto.request;

public record MemberUpdateRequest(
        String nickname,
        String phone
) {
}