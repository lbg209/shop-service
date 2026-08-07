package com.lbg0146.shop_service.member.dto.response;

import com.lbg0146.shop_service.member.entity.Member;

public record MemberResponse(
        Long memberId,
        String loginId,
        String name,
        String nickname,
        String email,
        String phone,
        String gradeName
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getLoginId(),
                member.getName(),
                member.getNickname(),
                member.getEmail(),
                member.getPhone(),
                member.getGrade().getGradeName()
        );
    }
}