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
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            log.warn("StompHeaderAccessor is null");
            return message;
        }

        // accessor를 mutable로 설정
        accessor.setLeaveMutable(true);

        StompCommand command = accessor.getCommand();
        log.info("STOMP Command 수신: {}, isMutable={}", command, accessor.isMutable());

        if (StompCommand.CONNECT.equals(command)) {
            handleConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            log.info("SUBSCRIBE 요청: destination={}, user={}",
                    accessor.getDestination(),
                    accessor.getUser() != null ? accessor.getUser().getName() : "null");
        } else if (StompCommand.SEND.equals(command)) {
            log.info("SEND 요청: destination={}, user={}",
                    accessor.getDestination(),
                    accessor.getUser() != null ? accessor.getUser().getName() : "null");
        } else if (StompCommand.DISCONNECT.equals(command)) {
            log.info("DISCONNECT 요청: user={}",
                    accessor.getUser() != null ? accessor.getUser().getName() : "null");
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        log.info("=== STOMP CONNECT 처리 시작 ===");

        // 모든 헤더 출력 (디버깅용)
        if (accessor.toNativeHeaderMap() != null) {
            log.info("Native Headers: {}", accessor.toNativeHeaderMap());
        }

        String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        log.info("Authorization Header: {}", authHeader);

        if (authHeader == null || authHeader.isBlank()) {
            log.error("인증 헤더가 없습니다.");
            throw new UnauthorizedException();
        }

        String token = extractToken(authHeader);
        log.info("추출된 토큰: {}...", token.length() > 20 ? token.substring(0, 20) : token);

        try {
            // JWT 토큰 검증
            jwtTokenProvider.validateToken(token);

            // 토큰에서 userId 추출
            Long userId = jwtTokenProvider.getUserIdFromToken(token);

            // Principal 설정
            Principal principal = new StompPrincipal(userId.toString());
            accessor.setUser(principal);

            log.info("=== STOMP CONNECT 성공: userId={} ===", userId);
        } catch (Exception e) {
            log.error("토큰 검증 실패: {}", e.getMessage(), e);
            throw new InvalidTokenException();
        }
    }

    private String extractToken(String authHeader) {
        String trimmedHeader = authHeader.trim();

        // "Bearer " 접두사 확인 (대소문자 무시)
        if (trimmedHeader.toLowerCase().startsWith(BEARER_PREFIX.toLowerCase())) {
            return trimmedHeader.substring(BEARER_PREFIX.length()).trim();
        }

        // Bearer 없이 토큰만 온 경우도 허용 (테스트 편의)
        log.warn("Bearer 접두사 없이 토큰 수신, 토큰으로 직접 처리");
        return trimmedHeader;
    }
}
