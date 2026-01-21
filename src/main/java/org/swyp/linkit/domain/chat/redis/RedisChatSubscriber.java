package org.swyp.linkit.domain.chat.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.swyp.linkit.domain.chat.dto.response.ChatPayloadResponseDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            String channel = new String(message.getChannel());

            ChatPayloadResponseDto payload = objectMapper.readValue(body, ChatPayloadResponseDto.class);
            String dest = "/topic/chat.room." + payload.getRoomId();
            messagingTemplate.convertAndSend(dest, payload);

            log.info("Redis -> WebSocket 브로드캐스트: channel={}, roomId={}", channel, payload.getRoomId());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Redis 메시지 역직렬화 실패", e);
        } catch (Exception e) {
            log.error("Redis 메시지 처리 중 알 수 없는 오류 발생", e);
        }
    }
}