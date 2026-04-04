package org.swyp.linkit.domain.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.swyp.linkit.domain.chat.dto.ChatMessageDto;
import org.swyp.linkit.domain.chat.entity.ChatMessage;
import org.swyp.linkit.domain.chat.entity.ChatRoom;
import org.swyp.linkit.domain.chat.entity.SenderRole;
import org.swyp.linkit.domain.chat.repository.ChatMessageDeleteRepository;
import org.swyp.linkit.domain.chat.repository.ChatMessageRepository;
import org.swyp.linkit.domain.chat.repository.ChatReadRepository;
import org.swyp.linkit.domain.chat.repository.ChatRoomRepository;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.notification.service.NotificationService;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.error.exception.ChatNotParticipantException;
import org.swyp.linkit.global.error.exception.ChatRoomNotFoundException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService 테스트")
class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatReadRepository chatReadRepository;

    @Mock
    private ChatMessageDeleteRepository chatMessageDeleteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private NotificationService notificationService;

    private ChatRoom chatRoom;
    private Long mentorId;
    private Long menteeId;
    private Long roomId;

    @BeforeEach
    void setUp() {
        mentorId = 1L;
        menteeId = 2L;
        roomId = 100L;
        chatRoom = createChatRoom(roomId, mentorId, menteeId);
    }

    @Nested
    @DisplayName("참여자 검증")
    class AssertParticipant {

        @Test
        @DisplayName("참여자인 경우 예외가 발생하지 않는다")
        void assertParticipant_success() {
            // given
            given(chatRoomRepository.findByIdWithUsers(roomId)).willReturn(Optional.of(chatRoom));
            given(chatRoomRepository.existsByIdAndUserId(roomId, mentorId)).willReturn(true);

            // when & then
            chatService.assertParticipant(mentorId, roomId);
        }

        @Test
        @DisplayName("참여자가 아닌 경우 예외가 발생한다")
        void assertParticipant_fail() {
            // given
            Long nonParticipantId = 999L;
            given(chatRoomRepository.findByIdWithUsers(roomId)).willReturn(Optional.of(chatRoom));
            given(chatRoomRepository.existsByIdAndUserId(roomId, nonParticipantId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> chatService.assertParticipant(nonParticipantId, roomId))
                    .isInstanceOf(ChatNotParticipantException.class);
        }
    }

    @Nested
    @DisplayName("메시지 저장")
    class SaveMessage {

        @Test
        @DisplayName("멘토가 메시지를 저장한다")
        void saveMessage_mentor() {
            // given
            String content = "안녕하세요, 멘토입니다.";
            ChatMessage savedMessage = createChatMessage(1L, chatRoom, mentorId, SenderRole.MENTOR, content);

            User mockSender = createMockUser(mentorId);
            given(chatRoomRepository.findByIdWithUsers(roomId)).willReturn(Optional.of(chatRoom));
            given(userRepository.findById(mentorId)).willReturn(Optional.of(mockSender));
            given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);

            // when
            ChatMessage result = chatService.saveMessage(roomId, mentorId, content);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getSenderId()).isEqualTo(mentorId);
            assertThat(result.getSenderRole()).isEqualTo(SenderRole.MENTOR);
            assertThat(result.getContent()).isEqualTo(content);
            verify(chatMessageRepository).save(any(ChatMessage.class));
        }

        @Test
        @DisplayName("멘티가 메시지를 저장한다")
        void saveMessage_mentee() {
            // given
            String content = "안녕하세요, 멘티입니다.";
            ChatMessage savedMessage = createChatMessage(2L, chatRoom, menteeId, SenderRole.MENTEE, content);

            User mockSender = createMockUser(menteeId);
            given(chatRoomRepository.findByIdWithUsers(roomId)).willReturn(Optional.of(chatRoom));
            given(userRepository.findById(menteeId)).willReturn(Optional.of(mockSender));
            given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);

            // when
            ChatMessage result = chatService.saveMessage(roomId, menteeId, content);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getSenderId()).isEqualTo(menteeId);
            assertThat(result.getSenderRole()).isEqualTo(SenderRole.MENTEE);
            assertThat(result.getContent()).isEqualTo(content);
        }

        @Test
        @DisplayName("존재하지 않는 채팅방에 메시지 저장 시 예외가 발생한다")
        void saveMessage_roomNotFound() {
            // given
            given(chatRoomRepository.findByIdWithUsers(roomId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> chatService.saveMessage(roomId, mentorId, "메시지"))
                    .isInstanceOf(ChatRoomNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("메시지 조회")
    class GetMessages {

        @Test
        @DisplayName("채팅방 메시지 목록을 조회한다")
        void getMessages_success() {
            // given
            ChatMessage message1 = createChatMessage(1L, chatRoom, mentorId, SenderRole.MENTOR, "첫 번째 메시지");
            ChatMessage message2 = createChatMessage(2L, chatRoom, menteeId, SenderRole.MENTEE, "두 번째 메시지");
            ChatMessage message3 = createChatMessage(3L, chatRoom, mentorId, SenderRole.MENTOR, "세 번째 메시지");
            List<ChatMessage> messages = List.of(message1, message2, message3);

            given(chatRoomRepository.findByIdWithUsers(roomId)).willReturn(Optional.of(chatRoom));
            given(chatRoomRepository.existsByIdAndUserId(roomId, mentorId)).willReturn(true);
            given(chatMessageDeleteRepository.findDeletedMessageIdsByUserId(mentorId)).willReturn(Collections.emptyList());
            given(chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId)).willReturn(messages);

            // when
            List<ChatMessageDto> result = chatService.getMessages(roomId, mentorId);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getContent()).isEqualTo("첫 번째 메시지");
            assertThat(result.get(1).getContent()).isEqualTo("두 번째 메시지");
            assertThat(result.get(2).getContent()).isEqualTo("세 번째 메시지");
        }

        @Test
        @DisplayName("삭제된 메시지는 제외하고 조회한다")
        void getMessages_excludeDeleted() {
            // given
            ChatMessage message1 = createChatMessage(1L, chatRoom, mentorId, SenderRole.MENTOR, "첫 번째 메시지");
            ChatMessage message2 = createChatMessage(2L, chatRoom, menteeId, SenderRole.MENTEE, "삭제된 메시지");
            ChatMessage message3 = createChatMessage(3L, chatRoom, mentorId, SenderRole.MENTOR, "세 번째 메시지");
            List<ChatMessage> messages = List.of(message1, message2, message3);

            given(chatRoomRepository.findByIdWithUsers(roomId)).willReturn(Optional.of(chatRoom));
            given(chatRoomRepository.existsByIdAndUserId(roomId, mentorId)).willReturn(true);
            given(chatMessageDeleteRepository.findDeletedMessageIdsByUserId(mentorId)).willReturn(List.of(2L));
            given(chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId)).willReturn(messages);

            // when
            List<ChatMessageDto> result = chatService.getMessages(roomId, mentorId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getContent()).isEqualTo("첫 번째 메시지");
            assertThat(result.get(1).getContent()).isEqualTo("세 번째 메시지");
        }

        @Test
        @DisplayName("참여자가 아닌 경우 메시지 조회 시 예외가 발생한다")
        void getMessages_notParticipant() {
            // given
            Long nonParticipantId = 999L;
            given(chatRoomRepository.findByIdWithUsers(roomId)).willReturn(Optional.of(chatRoom));
            given(chatRoomRepository.existsByIdAndUserId(roomId, nonParticipantId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> chatService.getMessages(roomId, nonParticipantId))
                    .isInstanceOf(ChatNotParticipantException.class);
        }

        @Test
        @DisplayName("메시지가 없는 채팅방은 빈 목록을 반환한다")
        void getMessages_empty() {
            // given
            given(chatRoomRepository.findByIdWithUsers(roomId)).willReturn(Optional.of(chatRoom));
            given(chatRoomRepository.existsByIdAndUserId(roomId, mentorId)).willReturn(true);
            given(chatMessageDeleteRepository.findDeletedMessageIdsByUserId(mentorId)).willReturn(Collections.emptyList());
            given(chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId)).willReturn(Collections.emptyList());

            // when
            List<ChatMessageDto> result = chatService.getMessages(roomId, mentorId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("최근 메시지 조회")
    class GetRecentMessages {

        @Test
        @DisplayName("최근 메시지를 제한된 개수만큼 조회한다")
        void getRecentMessages_success() {
            // given
            int limit = 2;
            ChatMessage message1 = createChatMessage(3L, chatRoom, mentorId, SenderRole.MENTOR, "세 번째");
            ChatMessage message2 = createChatMessage(2L, chatRoom, menteeId, SenderRole.MENTEE, "두 번째");
            ChatMessage message3 = createChatMessage(1L, chatRoom, mentorId, SenderRole.MENTOR, "첫 번째");
            List<ChatMessage> messages = List.of(message1, message2, message3);

            given(chatRoomRepository.findByIdWithUsers(roomId)).willReturn(Optional.of(chatRoom));
            given(chatRoomRepository.existsByIdAndUserId(roomId, mentorId)).willReturn(true);
            given(chatMessageDeleteRepository.findDeletedMessageIdsByUserId(mentorId)).willReturn(Collections.emptyList());
            given(chatMessageRepository.findTop50ByChatRoomIdOrderByIdDesc(roomId)).willReturn(messages);

            // when
            List<ChatMessageDto> result = chatService.getRecentMessages(roomId, mentorId, limit);

            // then
            assertThat(result).hasSize(2);
        }
    }

    // === Helper Methods ===

    private User createMockUser(Long id) {
        User user = Mockito.mock(User.class);
        Mockito.lenient().when(user.getId()).thenReturn(id);
        return user;
    }

    private ChatRoom createChatRoom(Long id, Long mentorId, Long menteeId) {
        User mentor = createMockUser(mentorId);
        User mentee = createMockUser(menteeId);
        ChatRoom room = ChatRoom.create(mentor, mentee);
        setField(room, "id", id);
        return room;
    }

    private ChatMessage createChatMessage(Long id, ChatRoom chatRoom, Long senderId, SenderRole senderRole, String content) {
        User sender = createMockUser(senderId);
        ChatMessage message = ChatMessage.create(chatRoom, sender, senderRole, content);
        setField(message, "id", id);
        setField(message, "createdAt", LocalDateTime.now());
        return message;
    }

    private void setField(Object target, String fieldName, Object value) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to set field: " + fieldName, e);
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }
}