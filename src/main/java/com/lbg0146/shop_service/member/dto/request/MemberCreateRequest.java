package com.lbg0146.shop_service.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberCreateRequest(
        @NotBlank(message = "로그인 아이디는 필수입니다.")
        @Size(max = 50)
        String loginId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 255)
        String password,

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 50)
        String name,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 50)
        String nickname,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 아닙니다.")
        @Size(max = 100)
        String email,

        @NotBlank(message = "전화번호는 필수입니다.")
        @Size(max = 20)
        String phone
) {
}