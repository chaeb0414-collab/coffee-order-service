package com.example.coffeeorderservice.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_CHARGE_AMOUNT(HttpStatus.BAD_REQUEST, "충전 금액이 올바르지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자의 포인트 계정을 찾을 수 없습니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
    INVALID_ORDER_QUANTITY(HttpStatus.BAD_REQUEST, "주문 수량이 올바르지 않습니다."),
    INVALID_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "주문 금액이 정수 범위를 초과합니다."),
    INSUFFICIENT_POINT(HttpStatus.BAD_REQUEST, "보유 포인트가 부족합니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
