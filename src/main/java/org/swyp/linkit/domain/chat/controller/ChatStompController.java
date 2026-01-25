package org.swyp.linkit.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.swyp.linkit.domain.chat.dto.request.ChatSendRequestDto;
import org.swyp.linkit.domain.chat.entity.ChatMessage;
import org.swyp.linkit.domain.chat.service.ChatService;

import java.security.Principal;

/**
 * WebSocket STOMP 채팅 컨트롤러
 *
 * 주의: @RequestMapping은 @MessageMapping에 영향을 주지 않습니다!
 * @MessageMapping에 전체 경로를 명시해야 합니다.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatService chatService;

    /**
     * 메시지 전송
     * 클라이언트 발행: /app/chat/send
     * 구독: /topic/chat.room.{roomId}
     */
    @MessageMapping("/chat/send")
    public void send(@Payload ChatSendRequestDto dto, Principal principal) {
        Long senderId = Long.parseLong(principal.getName());
        Long roomId = dto.getRoomId();

        log.info("메시지 수신: roomId={}, senderId={}", roomId, senderId);

        // 권한 체크 (room 참여자 여부)
        chatService.assertParticipant(senderId, roomId);

        // 1) DB 저장
        ChatMessage saved = chatService.saveMessage(roomId, senderId, dto.getText());

        // 2) Redis publish (모든 인스턴스 팬아웃)
        chatService.publishToRedis(saved);
    }

    /**
     * 채팅방 입장
     * 클라이언트 발행: /app/chat/room/{roomId}/enter
     */
    @MessageMapping("/chat/room/{roomId}/enter")
    public void enterRoom(@DestinationVariable Long roomId, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        log.info("채팅방 입장 요청: roomId={}, userId={}", roomId, userId);

        processReadAndNotify(roomId, userId);

        log.info("채팅방 입장 완료: roomId={}, userId={}", roomId, userId);
    }

    /**
     * 채팅방 퇴장
     * 클라이언트 발행: /app/chat/room/{roomId}/exit
     */
    @MessageMapping("/chat/room/{roomId}/exit")
    public void exitRoom(@DestinationVariable Long roomId, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        log.info("채팅방 퇴장 요청: roomId={}, userId={}", roomId, userId);

        try {
            chatService.markAsRead(roomId, userId);
        } catch (RuntimeException e) {
            log.warn("퇴장 시 읽음 처리 실패: roomId={}, userId={}", roomId, userId, e);
        }

        log.info("채팅방 퇴장 완료: roomId={}, userId={}", roomId, userId);
    }

    /**
     * 읽음 처리 요청
     * 클라이언트 발행: /app/chat/room/{roomId}/read
     */
    @MessageMapping("/chat/room/{roomId}/read")
    public void markAsRead(@DestinationVariable Long roomId, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        log.info("읽음 처리 요청: roomId={}, userId={}", roomId, userId);

        processReadAndNotify(roomId, userId);

        log.info("읽음 처리 완료: roomId={}, userId={}", roomId, userId);
    }

    /**
     * 읽음 처리 공통 로직 (권한 체크 + 읽음 처리 + Redis 이벤트 발행)
     */
    private void processReadAndNotify(Long roomId, Long userId) {
        chatService.assertParticipant(userId, roomId);
        chatService.markAsRead(roomId, userId);
        chatService.publishReadEvent(roomId, userId, null);
    }
}
