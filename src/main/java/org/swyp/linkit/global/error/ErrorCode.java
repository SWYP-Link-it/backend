package org.swyp.linkit.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력 값에 대한 검증을 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류입니다."),

    // 인증/인가
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "만료된 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A003", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A004", "접근 권한이 없습니다."),
    INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "A005", "유효하지 않은 사용자 상태입니다."),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "U002", "이미 존재하는 닉네임입니다."),

    // 사용자 스킬
    USER_SKILL_NOT_FOUND(HttpStatus.NOT_FOUND, "US001", "스킬을 찾을 수 없습니다."),

    // OAuth
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "O001", "지원하지 않는 OAuth 제공자입니다."),
    OAUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "O002", "OAuth 인증에 실패했습니다."),

    // 채팅
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CH001", "채팅방을 찾을 수 없습니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CH002", "메시지를 찾을 수 없습니다."),
    CHAT_NOT_PARTICIPANT(HttpStatus.FORBIDDEN, "CH003", "채팅방 참여자가 아닙니다."),
    CHAT_INVALID_MESSAGE(HttpStatus.BAD_REQUEST, "CH004", "해당 채팅방의 메시지가 아닙니다."),
    CHAT_INVALID_USER(HttpStatus.BAD_REQUEST, "CH005", "유효하지 않은 사용자입니다."),
    CHAT_SAME_USER(HttpStatus.BAD_REQUEST, "CH006", "멘토와 멘티는 서로 다른 사용자여야 합니다."),

    // 크레딧
    NOT_FOUND_CREDIT(HttpStatus.NOT_FOUND, "CR001", "크레딧 정보를 찾을 수 없습니다."),
    NOT_ENOUGH_CREDIT(HttpStatus.BAD_REQUEST, "CR002", "크레딧 잔액이 부족합니다."),
    INVALID_CREDIT_AMOUNT(HttpStatus.BAD_REQUEST, "CR003", "크레딧은 0보다 커야합니다."),

    // 알림
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "알림을 찾을 수 없습니다."),
    NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "N002", "해당 알림에 대한 접근 권한이 없습니다."),
    NOTIFICATION_ALREADY_READ(HttpStatus.BAD_REQUEST, "N003", "이미 읽은 알림입니다."),
    INVALID_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, "N004", "유효하지 않은 알림 타입입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
