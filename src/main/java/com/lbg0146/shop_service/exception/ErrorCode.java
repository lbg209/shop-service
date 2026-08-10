package com.lbg0146.shop_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),

    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 배송지입니다."),

    GRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "등급 정보를 찾을 수 없습니다."),

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),

    CATEGORY_HAS_CHILDREN(HttpStatus.BAD_REQUEST, "하위 카테고리가 존재하므로 삭제할 수 없습니다."),

    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니를 찾을 수 없습니다."),

    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니를 찾을 수 없습니다."),

    CART_EMPTY(HttpStatus.BAD_REQUEST, "장바구니가 비어있습니다."),

    INVALID_QUANTITY(HttpStatus.BAD_REQUEST,"상품 수량은 1개 이상이어야 합니다."),

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),

    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),

    DUPLICATE_PHONE(HttpStatus.CONFLICT, "이미 존재하는 전화번호입니다."),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),

    ORDER_ITEM_EMPTY(HttpStatus.BAD_REQUEST, "주문 상품이 없습니다."),

    ORDER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 상태를 찾을 수 없습니다."),

    ALREADY_PAID(HttpStatus.CONFLICT, "이미 결제가 완료된 주문입니다."),

    PAYMENT_METHOD_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 수단을 찾을 수 없습니다."),

    PAYMENT_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 상태를 찾을 수 없습니다."),

    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),

    ORDER_NOT_PAID(HttpStatus.BAD_REQUEST, "결제가 완료되지 않은 주문입니다."),

    ALREADY_DELIVERY(HttpStatus.CONFLICT, "이미 배송이 생성된 주문입니다."),

    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "배송을 찾을 수 없습니다."),

    DELIVERY_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "배송 상태를 찾을 수 없습니다."),

    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "쿠폰을 찾을 수 없습니다."),

    MEMBER_COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 쿠폰을 찾을 수 없습니다."),

    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "쿠폰이 만료되었습니다."),

    COUPON_NOT_USABLE(HttpStatus.BAD_REQUEST, "사용할 수 없는 쿠폰입니다."),

    INVALID_DISCOUNT_TYPE(HttpStatus.BAD_REQUEST,"유효하지 않은 할인 방식입니다."),

    INVALID_DISCOUNT_VALUE(HttpStatus.BAD_REQUEST, "할인 값이 올바르지 않습니다."),

    INVALID_MIN_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "최소 주문 금액이 올바르지 않습니다."),

    INVALID_MAX_DISCOUNT_AMOUNT(HttpStatus.BAD_REQUEST, "최대 할인 금액이 올바르지 않습니다."),

    INVALID_VALID_DAYS(HttpStatus.BAD_REQUEST, "쿠폰 사용 가능 기간이 올바르지 않습니다."),

    COUPON_ALREADY_ISSUED(HttpStatus.BAD_REQUEST, "이미 발급받은 쿠폰입니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
