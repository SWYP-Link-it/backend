package org.swyp.linkit.global.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;
import org.swyp.linkit.global.error.exception.InvalidTokenException;
import org.swyp.linkit.global.error.exception.UnauthorizedException;
import org.swyp.linkit.global.error.exception.base.BusinessException;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class StompErrorHandler extends StompSubProtocolErrorHandler {

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;

        log.error("STOMP 에러 발생: {}", cause.getMessage());

        // 인증 관련 예외 처리
        if (cause instanceof UnauthorizedException) {
            return createErrorMessage("UNAUTHORIZED", "인증이 필요합니다.");
        }

        if (cause instanceof InvalidTokenException) {
            return createErrorMessage("INVALID_TOKEN", "유효하지 않은 토큰입니다.");
        }

        // 기타 비즈니스 예외 처리
        if (cause instanceof BusinessException businessException) {
            return createErrorMessage(
                    businessException.getErrorCode().getCode(),
                    businessException.getMessage()
            );
        }

        // 기타 예외
        return createErrorMessage("ERROR", "서버 오류가 발생했습니다.");
    }

    private Message<byte[]> createErrorMessage(String errorCode, String errorMessage) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage(errorCode);
        accessor.setLeaveMutable(true);

        String body = String.format("{\"code\":\"%s\",\"message\":\"%s\"}", errorCode, errorMessage);

        return MessageBuilder.createMessage(
                body.getBytes(StandardCharsets.UTF_8),
                accessor.getMessageHeaders()
        );
    }
}