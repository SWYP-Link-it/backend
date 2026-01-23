package org.swyp.linkit.domain.notification.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.swyp.linkit.domain.notification.dto.NotificationMessageDto;

/**
 * Redis에서 알림 메시지를 수신하여 WebSocket으로 전달
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            String channel = new String(message.getChannel());

            NotificationMessageDto payload = objectMapper.readValue(body, NotificationMessageDto.class);

            // 특정 사용자에게 알림 전송: /topic/notification.{userId}
            String dest = "/topic/notification." + payload.getReceiverId();
            messagingTemplate.convertAndSend(dest, payload);

            log.info("Redis -> WebSocket 알림 브로드캐스트: channel={}, receiverId={}, type={}",
                    channel, payload.getReceiverId(), payload.getNotificationType());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Redis 알림 메시지 역직렬화 실패", e);
        } catch (Exception e) {
            log.error("Redis 알림 메시지 처리 중 오류 발생", e);
        }
    }
}