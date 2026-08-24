package org.example.bankramenserver.global.error.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    KAKAO_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "카카오 액세스 토큰 요청에 실패했습니다."),
    KAKAO_USER_INFO_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "카카오 사용자 정보 요청에 실패했습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 없습니다."),
    INVALID_STATE(HttpStatus.BAD_REQUEST, "유효하지 않은 state 값입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "유저 정보가 없습니다."),

    // Transaction
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "거래 내역을 찾을 수 없습니다."),

    // FCM
    FCM_CREDENTIALS_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 인증 정보가 설정되지 않았습니다."),
    FCM_SEND_FAILED(HttpStatus.BAD_GATEWAY, "FCM 푸시 알림 전송에 실패했습니다."),

    // Recurring Payment
    RECURRING_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "정기결제를 찾을 수 없습니다."),
    RECURRING_PAYMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인의 정기결제만 처리할 수 있습니다."),
    DUPLICATE_RECURRING_PAYMENT(HttpStatus.CONFLICT, "이미 등록된 정기결제입니다."),
    INVALID_RECURRING_PAYMENT_TRANSACTION(HttpStatus.BAD_REQUEST, "유효하지 않은 거래입니다."),

    // Common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
