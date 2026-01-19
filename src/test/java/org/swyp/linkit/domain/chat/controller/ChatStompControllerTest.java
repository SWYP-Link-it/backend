package org.swyp.linkit.domain.chat.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.swyp.linkit.domain.chat.dto.request.ChatSendRequestDto;
import org.swyp.linkit.domain.chat.entity.ChatMessage;
import org.swyp.linkit.domain.chat.entity.ChatRoom;
import org.swyp.linkit.domain.chat.entity.SenderRole;
import org.swyp.linkit.domain.chat.service.ChatService;
import org.swyp.linkit.global.config.StompPrincipal;
import org.swyp.linkit.global.error.exception.ChatNotParticipantException;

import java.lang.reflect.Field;
import java.security.Principal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatStompController 테스트")
class ChatStompControllerTest {

    @InjectMocks
    private ChatStompController chatStompController;

    @Mock
    private ChatService chatService;

    private Long senderId;
    private Long roomId;
    private Principal principal;

    @BeforeEach
    void setUp() {
        senderId = 1L;
        roomId = 100L;
        principal = new StompPrincipal(senderId.toString());
    }

    @Nested
    @DisplayName("메시지 전송")
    class SendMessage {

        @Test
        @DisplayName("메시지를 성공적으로 전송한다")
        void send_success() {
            // given
            String text = "안녕하세요!";
            ChatSendRequestDto dto = createChatSendRequestDto(roomId, text);
            ChatRoom chatRoom = createChatRoom(roomId, senderId, 2L);
            ChatMessage savedMessage = createChatMessage(1L, chatRoom, senderId, SenderRole.MENTOR, text);

            doNothing().when(chatService).assertParticipant(senderId, roomId);
            given(chatService.saveMessage(roomId, senderId, text)).willReturn(savedMessage);
            doNothing().when(chatService).publishToRedis(any(ChatMessage.class));

            // when
            chatStompController.send(dto, principal);

            // then
            verify(chatService).assertParticipant(senderId, roomId);
            verify(chatService).saveMessage(roomId, senderId, text);
            verify(chatService).publishToRedis(savedMessage);
        }

        @Test
        @DisplayName("참여자가 아닌 경우 메시지 전송 시 예외가 발생한다")
        void send_notParticipant() {
            // given
            String text = "안녕하세요!";
            ChatSendRequestDto dto = createChatSendRequestDto(roomId, text);

            doThrow(new ChatNotParticipantException(roomId, senderId))
                    .when(chatService).assertParticipant(senderId, roomId);

            // when & then
            assertThatThrownBy(() -> chatStompController.send(dto, principal))
                    .isInstanceOf(ChatNotParticipantException.class);

            verify(chatService).assertParticipant(senderId, roomId);
            verify(chatService, never()).saveMessage(anyLong(), anyLong(), anyString());
            verify(chatService, never()).publishToRedis(any());
        }

        @Test
        @DisplayName("멘토가 메시지를 전송한다")
        void send_asMentor() {
            // given
            Long mentorId = 1L;
            Long menteeId = 2L;
            String text = "멘토입니다. 반갑습니다!";
            Principal mentorPrincipal = new StompPrincipal(mentorId.toString());
            ChatSendRequestDto dto = createChatSendRequestDto(roomId, text);
            ChatRoom chatRoom = createChatRoom(roomId, mentorId, menteeId);
            ChatMessage savedMessage = createChatMessage(1L, chatRoom, mentorId, SenderRole.MENTOR, text);

            doNothing().when(chatService).assertParticipant(mentorId, roomId);
            given(chatService.saveMessage(roomId, mentorId, text)).willReturn(savedMessage);
            doNothing().when(chatService).publishToRedis(any(ChatMessage.class));

            // when
            chatStompController.send(dto, mentorPrincipal);

            // then
            verify(chatService).saveMessage(roomId, mentorId, text);
            verify(chatService).publishToRedis(savedMessage);
        }

        @Test
        @DisplayName("멘티가 메시지를 전송한다")
        void send_asMentee() {
            // given
            Long mentorId = 1L;
            Long menteeId = 2L;
            String text = "멘티입니다. 질문있어요!";
            Principal menteePrincipal = new StompPrincipal(menteeId.toString());
            ChatSendRequestDto dto = createChatSendRequestDto(roomId, text);
            ChatRoom chatRoom = createChatRoom(roomId, mentorId, menteeId);
            ChatMessage savedMessage = createChatMessage(2L, chatRoom, menteeId, SenderRole.MENTEE, text);

            doNothing().when(chatService).assertParticipant(menteeId, roomId);
            given(chatService.saveMessage(roomId, menteeId, text)).willReturn(savedMessage);
            doNothing().when(chatService).publishToRedis(any(ChatMessage.class));

            // when
            chatStompController.send(dto, menteePrincipal);

            // then
            verify(chatService).saveMessage(roomId, menteeId, text);
            verify(chatService).publishToRedis(savedMessage);
        }

        @Test
        @DisplayName("긴 메시지를 전송한다")
        void send_longMessage() {
            // given
            String longText = "안녕하세요! ".repeat(100);
            ChatSendRequestDto dto = createChatSendRequestDto(roomId, longText);
            ChatRoom chatRoom = createChatRoom(roomId, senderId, 2L);
            ChatMessage savedMessage = createChatMessage(1L, chatRoom, senderId, SenderRole.MENTOR, longText);

            doNothing().when(chatService).assertParticipant(senderId, roomId);
            given(chatService.saveMessage(roomId, senderId, longText)).willReturn(savedMessage);
            doNothing().when(chatService).publishToRedis(any(ChatMessage.class));

            // when
            chatStompController.send(dto, principal);

            // then
            verify(chatService).saveMessage(roomId, senderId, longText);
        }
    }

    // === Helper Methods ===

    private ChatSendRequestDto createChatSendRequestDto(Long roomId, String text) {
        ChatSendRequestDto dto = new ChatSendRequestDto();
        setField(dto, "roomId", roomId);
        setField(dto, "text", text);
        return dto;
    }

    private ChatRoom createChatRoom(Long id, Long mentorId, Long menteeId) {
        ChatRoom room = ChatRoom.create(mentorId, menteeId);
        setField(room, "id", id);
        return room;
    }

    private ChatMessage createChatMessage(Long id, ChatRoom chatRoom, Long senderId,
                                           SenderRole senderRole, String content) {
        ChatMessage message = ChatMessage.create(chatRoom, senderId, senderRole, content);
        setField(message, "id", id);
        setField(message, "createdAt", LocalDateTime.now());
        return message;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}