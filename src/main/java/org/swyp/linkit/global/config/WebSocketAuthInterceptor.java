package org.swyp.linkit.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.swyp.linkit.global.auth.jwt.JwtTokenProvider;
import org.swyp.linkit.global.error.exception.InvalidTokenException;
import org.swyp.linkit.global.error.exception.UnauthorizedException;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
            log.debug("WebSocket CONNECT 요청, Authorization: {}", authHeader);

            String token = extractToken(authHeader);

            try {
                // JWT 토큰 검증
                jwtTokenProvider.validateToken(token);

                // 토큰에서 userId 추출
                Long userId = jwtTokenProvider.getUserIdFromToken(token);

                // Principal 설정
                Principal principal = new StompPrincipal(userId.toString());
                accessor.setUser(principal);

                log.info("WebSocket 인증 성공: userId={}", userId);
            } catch (InvalidTokenException e) {
                log.error("WebSocket 인증 실패 - 유효하지 않은 토큰: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("WebSocket 인증 실패: {}", e.getMessage());
                throw new InvalidTokenException();
            }
        }

        return message;
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            log.warn("WebSocket 연결 시 인증 헤더가 없습니다.");
            throw new UnauthorizedException();
        }

        // 대소문자 무시하고 "Bearer" 접두사 확인
        String trimmedHeader = authHeader.trim();
        if (!trimmedHeader.toLowerCase().startsWith(BEARER_PREFIX.toLowerCase())) {
            log.warn("WebSocket 인증 헤더 형식 오류: {}", authHeader);
            throw new InvalidTokenException();
        }

        // "Bearer" 이후 토큰 추출 (공백 처리)
        String token = trimmedHeader.substring(BEARER_PREFIX.length()).trim();

        if (token.isEmpty()) {
            log.warn("WebSocket 인증 토큰이 비어있습니다.");
            throw new InvalidTokenException();
        }

        return token;
    }
}
