package com.lbg0146.shop_service.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    MEMBER_NOT_FOUND("회원을 찾을 수 없습니다."),
    GRADE_NOT_FOUND("등급 정보를 찾을 수 없습니다."),

    DUPLICATE_LOGIN_ID("이미 존재하는 아이디입니다."),
    DUPLICATE_EMAIL("이미 존재하는 이메일입니다."),
    DUPLICATE_PHONE("이미 존재하는 전화번호입니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
