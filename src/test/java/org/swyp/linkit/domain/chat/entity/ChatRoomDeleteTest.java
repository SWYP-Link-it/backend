package org.swyp.linkit.domain.chat.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatRoomDelete 엔티티 테스트")
class ChatRoomDeleteTest {

    @Nested
    @DisplayName("채팅방 삭제 기록 생성")
    class CreateChatRoomDelete {

        @Test
        @DisplayName("채팅방과 사용자 ID로 삭제 기록을 생성한다")
        void create() {
            // given
            ChatRoom chatRoom = ChatRoom.create(1L, 2L);
            Long userId = 1L;

            // when
            ChatRoomDelete chatRoomDelete = ChatRoomDelete.create(chatRoom, userId);

            // then
            assertThat(chatRoomDelete.getChatRoom()).isEqualTo(chatRoom);
            assertThat(chatRoomDelete.getId().getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("멘토가 채팅방을 삭제한 기록을 생성한다")
        void create_mentor() {
            // given
            Long mentorId = 1L;
            Long menteeId = 2L;
            ChatRoom chatRoom = ChatRoom.create(mentorId, menteeId);

            // when
            ChatRoomDelete chatRoomDelete = ChatRoomDelete.create(chatRoom, mentorId);

            // then
            assertThat(chatRoomDelete.getId().getUserId()).isEqualTo(mentorId);
            assertThat(chatRoomDelete.getChatRoom()).isEqualTo(chatRoom);
        }

        @Test
        @DisplayName("멘티가 채팅방을 삭제한 기록을 생성한다")
        void create_mentee() {
            // given
            Long mentorId = 1L;
            Long menteeId = 2L;
            ChatRoom chatRoom = ChatRoom.create(mentorId, menteeId);

            // when
            ChatRoomDelete chatRoomDelete = ChatRoomDelete.create(chatRoom, menteeId);

            // then
            assertThat(chatRoomDelete.getId().getUserId()).isEqualTo(menteeId);
            assertThat(chatRoomDelete.getChatRoom()).isEqualTo(chatRoom);
        }

        @Test
        @DisplayName("동일한 채팅방에 대해 멘토와 멘티 각각 삭제 기록을 생성할 수 있다")
        void create_bothUsers() {
            // given
            Long mentorId = 1L;
            Long menteeId = 2L;
            ChatRoom chatRoom = ChatRoom.create(mentorId, menteeId);

            // when
            ChatRoomDelete mentorDelete = ChatRoomDelete.create(chatRoom, mentorId);
            ChatRoomDelete menteeDelete = ChatRoomDelete.create(chatRoom, menteeId);

            // then
            assertThat(mentorDelete.getId().getUserId()).isEqualTo(mentorId);
            assertThat(menteeDelete.getId().getUserId()).isEqualTo(menteeId);
            assertThat(mentorDelete.getChatRoom()).isEqualTo(menteeDelete.getChatRoom());
        }
    }

    @Nested
    @DisplayName("ChatRoomDeleteId 복합키 테스트")
    class ChatRoomDeleteIdTest {

        @Test
        @DisplayName("동일한 chatRoomId와 userId를 가진 ChatRoomDeleteId는 동등하다")
        void equals_sameId() {
            // given
            ChatRoomDeleteId id1 = new ChatRoomDeleteId(1L, 2L);
            ChatRoomDeleteId id2 = new ChatRoomDeleteId(1L, 2L);

            // then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 chatRoomId를 가진 ChatRoomDeleteId는 동등하지 않다")
        void equals_differentChatRoomId() {
            // given
            ChatRoomDeleteId id1 = new ChatRoomDeleteId(1L, 2L);
            ChatRoomDeleteId id2 = new ChatRoomDeleteId(3L, 2L);

            // then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("다른 userId를 가진 ChatRoomDeleteId는 동등하지 않다")
        void equals_differentUserId() {
            // given
            ChatRoomDeleteId id1 = new ChatRoomDeleteId(1L, 2L);
            ChatRoomDeleteId id2 = new ChatRoomDeleteId(1L, 3L);

            // then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("ChatRoomDeleteId의 getter가 올바르게 동작한다")
        void getter() {
            // given
            Long chatRoomId = 1L;
            Long userId = 2L;

            // when
            ChatRoomDeleteId id = new ChatRoomDeleteId(chatRoomId, userId);

            // then
            assertThat(id.getChatRoomId()).isEqualTo(chatRoomId);
            assertThat(id.getUserId()).isEqualTo(userId);
        }
    }
}
