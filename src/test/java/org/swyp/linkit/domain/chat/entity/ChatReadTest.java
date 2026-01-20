package org.swyp.linkit.domain.chat.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatRead 엔티티 테스트")
class ChatReadTest {

    @Nested
    @DisplayName("읽음 기록 생성")
    class CreateChatRead {

        @Test
        @DisplayName("채팅방과 사용자 ID, 마지막 읽은 메시지 ID로 읽음 기록을 생성한다")
        void create() {
            // given
            ChatRoom chatRoom = ChatRoom.create(1L, 2L);
            Long userId = 1L;
            Long lastReadMessageId = 100L;

            // when
            ChatRead chatRead = ChatRead.create(chatRoom, userId, lastReadMessageId);

            // then
            assertThat(chatRead.getChatRoom()).isEqualTo(chatRoom);
            assertThat(chatRead.getId().getUserId()).isEqualTo(userId);
            assertThat(chatRead.getLastReadMessageId()).isEqualTo(lastReadMessageId);
        }

        @Test
        @DisplayName("멘토의 읽음 기록을 생성한다")
        void create_mentor() {
            // given
            Long mentorId = 1L;
            Long menteeId = 2L;
            ChatRoom chatRoom = ChatRoom.create(mentorId, menteeId);
            Long lastReadMessageId = 50L;

            // when
            ChatRead chatRead = ChatRead.create(chatRoom, mentorId, lastReadMessageId);

            // then
            assertThat(chatRead.getId().getUserId()).isEqualTo(mentorId);
            assertThat(chatRead.getLastReadMessageId()).isEqualTo(lastReadMessageId);
        }

        @Test
        @DisplayName("멘티의 읽음 기록을 생성한다")
        void create_mentee() {
            // given
            Long mentorId = 1L;
            Long menteeId = 2L;
            ChatRoom chatRoom = ChatRoom.create(mentorId, menteeId);
            Long lastReadMessageId = 75L;

            // when
            ChatRead chatRead = ChatRead.create(chatRoom, menteeId, lastReadMessageId);

            // then
            assertThat(chatRead.getId().getUserId()).isEqualTo(menteeId);
            assertThat(chatRead.getLastReadMessageId()).isEqualTo(lastReadMessageId);
        }
    }

    @Nested
    @DisplayName("마지막 읽은 메시지 업데이트")
    class UpdateLastReadMessage {

        @Test
        @DisplayName("마지막 읽은 메시지를 업데이트한다")
        void updateLastReadMessage() {
            // given
            ChatRoom chatRoom = ChatRoom.create(1L, 2L);
            ChatRead chatRead = ChatRead.create(chatRoom, 1L, 100L);
            Long newLastReadMessageId = 200L;

            // when
            chatRead.updateLastReadMessage(newLastReadMessageId);

            // then
            assertThat(chatRead.getLastReadMessageId()).isEqualTo(newLastReadMessageId);
        }

        @Test
        @DisplayName("마지막 읽은 메시지를 여러 번 업데이트할 수 있다")
        void updateLastReadMessage_multiple() {
            // given
            ChatRoom chatRoom = ChatRoom.create(1L, 2L);
            ChatRead chatRead = ChatRead.create(chatRoom, 1L, 100L);

            // when
            chatRead.updateLastReadMessage(150L);
            chatRead.updateLastReadMessage(200L);
            chatRead.updateLastReadMessage(250L);

            // then
            assertThat(chatRead.getLastReadMessageId()).isEqualTo(250L);
        }
    }

    @Nested
    @DisplayName("ChatReadId 복합키 테스트")
    class ChatReadIdTest {

        @Test
        @DisplayName("동일한 chatRoomId와 userId를 가진 ChatReadId는 동등하다")
        void equals_sameId() {
            // given
            ChatReadId id1 = new ChatReadId(1L, 2L);
            ChatReadId id2 = new ChatReadId(1L, 2L);

            // then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 chatRoomId를 가진 ChatReadId는 동등하지 않다")
        void equals_differentChatRoomId() {
            // given
            ChatReadId id1 = new ChatReadId(1L, 2L);
            ChatReadId id2 = new ChatReadId(3L, 2L);

            // then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("다른 userId를 가진 ChatReadId는 동등하지 않다")
        void equals_differentUserId() {
            // given
            ChatReadId id1 = new ChatReadId(1L, 2L);
            ChatReadId id2 = new ChatReadId(1L, 3L);

            // then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("ChatReadId의 getter가 올바르게 동작한다")
        void getter() {
            // given
            Long chatRoomId = 1L;
            Long userId = 2L;

            // when
            ChatReadId id = new ChatReadId(chatRoomId, userId);

            // then
            assertThat(id.getChatRoomId()).isEqualTo(chatRoomId);
            assertThat(id.getUserId()).isEqualTo(userId);
        }
    }
}
