package org.swyp.linkit.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.swyp.linkit.global.error.annotation.ExplainError;
import org.swyp.linkit.global.error.code.BaseErrorCode;
import org.swyp.linkit.global.error.dto.ErrorReason;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseErrorCode {

    // 공통
    @ExplainError("요청 파라미터나 바디의 유효성 검증에 실패한 경우 발생합니다.")
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력 값에 대한 검증을 실패했습니다."),

    @ExplainError("예상치 못한 서버 내부 오류가 발생한 경우입니다.")
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류입니다."),

    // 인증/인가
    @ExplainError("토큰 형식이 잘못되었거나 서명 검증에 실패한 경우 발생합니다.")
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 토큰입니다."),

    @ExplainError("토큰의 유효기간이 만료된 경우 발생합니다.")
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "만료된 토큰입니다."),

    @ExplainError("인증 정보가 없거나 인증에 실패한 경우 발생합니다.")
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A003", "인증이 필요합니다."),

    @ExplainError("해당 리소스에 대한 접근 권한이 없는 경우 발생합니다.")
    FORBIDDEN(HttpStatus.FORBIDDEN, "A004", "접근 권한이 없습니다."),

    @ExplainError("탈퇴했거나 정지된 사용자 등 유효하지 않은 사용자 상태인 경우 발생합니다.")
    INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "A005", "유효하지 않은 사용자 상태입니다."),

    // 사용자
    @ExplainError("요청한 사용자 ID에 해당하는 사용자가 존재하지 않는 경우 발생합니다.")
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),

    @ExplainError("이미 다른 사용자가 사용 중인 닉네임으로 변경하려는 경우 발생합니다.")
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "U002", "이미 존재하는 닉네임입니다."),

    // 사용자 스킬
    @ExplainError("요청한 스킬 ID에 해당하는 스킬이 존재하지 않는 경우 발생합니다.")
    USER_SKILL_NOT_FOUND(HttpStatus.NOT_FOUND, "US001", "스킬을 찾을 수 없습니다."),

    // OAuth
    @ExplainError("지원하지 않는 OAuth 제공자(카카오, 네이버 등)를 요청한 경우 발생합니다.")
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "O001", "지원하지 않는 OAuth 제공자입니다."),

    @ExplainError("OAuth 인증 과정에서 오류가 발생한 경우 발생합니다.")
    OAUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "O002", "OAuth 인증에 실패했습니다."),

    // 채팅
    @ExplainError("요청한 채팅방 ID에 해당하는 채팅방이 존재하지 않는 경우 발생합니다.")
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CH001", "채팅방을 찾을 수 없습니다."),

    @ExplainError("요청한 메시지 ID에 해당하는 메시지가 존재하지 않는 경우 발생합니다.")
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CH002", "메시지를 찾을 수 없습니다."),

    @ExplainError("채팅방의 참여자가 아닌 사용자가 접근하려는 경우 발생합니다.")
    CHAT_NOT_PARTICIPANT(HttpStatus.FORBIDDEN, "CH003", "채팅방 참여자가 아닙니다."),

    @ExplainError("요청한 메시지가 해당 채팅방의 메시지가 아닌 경우 발생합니다.")
    CHAT_INVALID_MESSAGE(HttpStatus.BAD_REQUEST, "CH004", "해당 채팅방의 메시지가 아닙니다."),

    @ExplainError("채팅 요청에 포함된 사용자가 유효하지 않은 경우 발생합니다.")
    CHAT_INVALID_USER(HttpStatus.BAD_REQUEST, "CH005", "유효하지 않은 사용자입니다."),

    @ExplainError("멘토와 멘티가 동일한 사용자인 경우 발생합니다.")
    CHAT_SAME_USER(HttpStatus.BAD_REQUEST, "CH006", "멘토와 멘티는 서로 다른 사용자여야 합니다."),

    // 크레딧
    @ExplainError("사용자의 크레딧 정보가 존재하지 않는 경우 발생합니다.")
    NOT_FOUND_CREDIT(HttpStatus.NOT_FOUND, "CR001", "크레딧 정보를 찾을 수 없습니다."),

    @ExplainError("크레딧 잔액이 요청한 금액보다 부족한 경우 발생합니다.")
    NOT_ENOUGH_CREDIT(HttpStatus.BAD_REQUEST, "CR002", "크레딧 잔액이 부족합니다."),

    @ExplainError("크레딧 금액이 0 이하인 경우 발생합니다.")
    INVALID_CREDIT_AMOUNT(HttpStatus.BAD_REQUEST, "CR003", "크레딧은 0보다 커야합니다."),

    // 스킬 교환
    @ExplainError("멘토의 스케줄이 존재하지 않는 경우 발생합니다.")
    SCHEDULE_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "EX001", "멘토의 스케줄 정보를 찾을 수 없습니다."),

    @ExplainError("멘토의 정보가 존재하지 않는 경우 발생합니다.")
    MENTOR_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "EX002", "멘토를 찾을 수 없습니다."),

    // 알림
    @ExplainError("요청한 알림 ID에 해당하는 알림이 존재하지 않는 경우 발생합니다.")
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "알림을 찾을 수 없습니다."),

    @ExplainError("다른 사용자의 알림에 접근하려는 경우 발생합니다.")
    NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "N002", "해당 알림에 대한 접근 권한이 없습니다."),

    @ExplainError("이미 읽음 처리된 알림을 다시 읽음 처리하려는 경우 발생합니다.")
    NOTIFICATION_ALREADY_READ(HttpStatus.BAD_REQUEST, "N003", "이미 읽은 알림입니다."),

    @ExplainError("지원하지 않는 알림 타입을 요청한 경우 발생합니다.")
    INVALID_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, "N004", "유효하지 않은 알림 타입입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.builder()
                .status(httpStatus.value())
                .code(code)
                .reason(message)
                .build();
    }
}
