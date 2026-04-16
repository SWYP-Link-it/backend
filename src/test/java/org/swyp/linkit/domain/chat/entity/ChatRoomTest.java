package org.swyp.linkit.domain.chat.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.swyp.linkit.domain.user.entity.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ChatRoom 엔티티 테스트")
class ChatRoomTest {

    @Nested
    @DisplayName("채팅방 생성")
    class CreateChatRoom {

        @Test
        @DisplayName("멘토와 멘티 ID로 채팅방을 생성한다")
        void create() {
            // given
            Long mentorId = 1L;
            Long menteeId = 2L;

            // when
            ChatRoom chatRoom = ChatRoom.create(createMockUser(mentorId), createMockUser(menteeId));

            // then
            assertThat(chatRoom.getMentorId()).isEqualTo(mentorId);
            assertThat(chatRoom.getMenteeId()).isEqualTo(menteeId);
        }

        @Test
        @DisplayName("채팅방 생성 시 기본 상태는 OPEN이다")
        void create_defaultStatus() {
            // when
            ChatRoom chatRoom = createDefaultChatRoom();

            // then
            assertThat(chatRoom.getStatus()).isEqualTo(ChatRoomStatus.OPEN);
        }

        @Test
        @DisplayName("채팅방 생성 시 읽지 않은 메시지 수는 0이다")
        void create_defaultUnreadCount() {
            // when
            ChatRoom chatRoom = createDefaultChatRoom();

            // then
            assertThat(chatRoom.getUnreadMentorCount()).isZero();
            assertThat(chatRoom.getUnreadMenteeCount()).isZero();
        }

        @Test
        @DisplayName("채팅방 생성 시 마지막 메시지 정보는 null이다")
        void create_nullLastMessage() {
            // when
            ChatRoom chatRoom = createDefaultChatRoom();

            // then
            assertThat(chatRoom.getLastMessageId()).isNull();
            assertThat(chatRoom.getLastMessageAt()).isNull();
        }
    }

    @Nested
    @DisplayName("마지막 메시지 업데이트")
    class UpdateLastMessage {

        @Test
        @DisplayName("마지막 메시지 정보를 업데이트한다")
        void updateLastMessage() {
            // given
            ChatRoom chatRoom = createDefaultChatRoom();
            Long messageId = 100L;
            java.time.LocalDateTime messageAt = java.time.LocalDateTime.now();

            // when
            chatRoom.updateLastMessage(messageId, messageAt);

            // then
            assertThat(chatRoom.getLastMessageId()).isEqualTo(messageId);
            assertThat(chatRoom.getLastMessageAt()).isEqualTo(messageAt);
        }

        @Test
        @DisplayName("마지막 메시지 정보를 여러 번 업데이트할 수 있다")
        void updateLastMessage_multiple() {
            // given
            ChatRoom chatRoom = createDefaultChatRoom();
            java.time.LocalDateTime firstMessageAt = java.time.LocalDateTime.now();
            java.time.LocalDateTime secondMessageAt = firstMessageAt.plusMinutes(5);

            // when
            chatRoom.updateLastMessage(100L, firstMessageAt);
            chatRoom.updateLastMessage(200L, secondMessageAt);

            // then
            assertThat(chatRoom.getLastMessageId()).isEqualTo(200L);
            assertThat(chatRoom.getLastMessageAt()).isEqualTo(secondMessageAt);
        }
    }

    @Nested
    @DisplayName("채팅방 상태 변경")
    class ChangeStatus {

        @Test
        @DisplayName("채팅방 상태를 CLOSED로 변경한다")
        void changeStatus_toClosed() {
            // given
            ChatRoom chatRoom = createDefaultChatRoom();

            // when
            chatRoom.changeStatus(ChatRoomStatus.CLOSED);

            // then
            assertThat(chatRoom.getStatus()).isEqualTo(ChatRoomStatus.CLOSED);
        }

        @Test
        @DisplayName("채팅방 상태를 OPEN으로 다시 변경한다")
        void changeStatus_toOpen() {
            // given
            ChatRoom chatRoom = createDefaultChatRoom();
            chatRoom.changeStatus(ChatRoomStatus.CLOSED);

            // when
            chatRoom.changeStatus(ChatRoomStatus.OPEN);

            // then
            assertThat(chatRoom.getStatus()).isEqualTo(ChatRoomStatus.OPEN);
        }
    }

    private User createMockUser(Long id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        return user;
    }

    private ChatRoom createDefaultChatRoom() {
        return ChatRoom.create(createMockUser(1L), createMockUser(2L));
    }
}
