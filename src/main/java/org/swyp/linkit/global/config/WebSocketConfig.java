package org.swyp.linkit.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.swyp.linkit.global.handler.StompErrorHandler;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    private final StompErrorHandler stompErrorHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독할 prefix (서버 -> 클라이언트)
        registry.enableSimpleBroker("/topic", "/queue");

        // 클라이언트가 메시지를 보낼 prefix (클라이언트 -> 서버)
        registry.setApplicationDestinationPrefixes("/app");

        // 특정 사용자에게 메시지 전송 시 prefix
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트 (SockJS fallback 포함)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "{FRONTEND_URL}",      // 로컬 개발
                        "{FRONTEND_PROD_URL}",       // Vercel 프리뷰/배포
                        "{BACKEND_URL}",         // 운영 도메인
                        "{BACKEND_PROD_URL}"        // 서브도메인
                )
                .withSockJS();

        // STOMP 에러 핸들러 등록
        registry.setErrorHandler(stompErrorHandler);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 인증 인터셉터 등록
        registration.interceptors(webSocketAuthInterceptor);
    }
}