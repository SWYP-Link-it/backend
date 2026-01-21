package org.swyp.linkit.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.swyp.linkit.domain.chat.dto.request.ChatSendRequestDto;
import org.swyp.linkit.domain.chat.entity.ChatMessage;
import org.swyp.linkit.domain.chat.service.ChatService;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatStompController {

    private final ChatService chatService;

    /**
     * 클라이언트 발행: /app/chat/send
     * 메시지 전송 처리
     */
    @MessageMapping("/send")
    public void send(ChatSendRequestDto dto, Principal principal) {
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
}