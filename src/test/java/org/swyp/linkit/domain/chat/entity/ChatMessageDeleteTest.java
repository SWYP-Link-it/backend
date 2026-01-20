package org.swyp.linkit.domain.chat.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatMessageDelete 엔티티 테스트")
class ChatMessageDeleteTest {

    @Nested
    @DisplayName("메시지 삭제 기록 생성")
    class CreateChatMessageDelete {

        @Test
        @DisplayName("메시지와 사용자 ID로 삭제 기록을 생성한다")
        void create() {
            // given
            ChatRoom chatRoom = createDefaultChatRoom();
            ChatMessage chatMessage = ChatMessage.create(chatRoom, 1L, SenderRole.MENTOR, "테스트 메시지");
            Long userId = 1L;

            // when
            ChatMessageDelete chatMessageDelete = ChatMessageDelete.create(chatMessage, userId);

            // then
            assertThat(chatMessageDelete.getChatMessage()).isEqualTo(chatMessage);
            assertThat(chatMessageDelete.getId().getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("멘토가 메시지를 삭제한 기록을 생성한다")
        void create_mentor() {
            // given
            Long mentorId = 1L;
            ChatRoom chatRoom = createDefaultChatRoom();
            ChatMessage chatMessage = ChatMessage.create(chatRoom, mentorId, SenderRole.MENTOR, "멘토 메시지");

            // when
            ChatMessageDelete chatMessageDelete = ChatMessageDelete.create(chatMessage, mentorId);

            // then
            assertThat(chatMessageDelete.getId().getUserId()).isEqualTo(mentorId);
            assertThat(chatMessageDelete.getChatMessage()).isEqualTo(chatMessage);
        }

        @Test
        @DisplayName("멘티가 메시지를 삭제한 기록을 생성한다")
        void create_mentee() {
            // given
            Long menteeId = 2L;
            ChatRoom chatRoom = createDefaultChatRoom();
            ChatMessage chatMessage = ChatMessage.create(chatRoom, menteeId, SenderRole.MENTEE, "멘티 메시지");

            // when
            ChatMessageDelete chatMessageDelete = ChatMessageDelete.create(chatMessage, menteeId);

            // then
            assertThat(chatMessageDelete.getId().getUserId()).isEqualTo(menteeId);
            assertThat(chatMessageDelete.getChatMessage()).isEqualTo(chatMessage);
        }

        @Test
        @DisplayName("동일한 메시지에 대해 멘토와 멘티 각각 삭제 기록을 생성할 수 있다")
        void create_bothUsers() {
            // given
            Long mentorId = 1L;
            Long menteeId = 2L;
            ChatRoom chatRoom = createDefaultChatRoom();
            ChatMessage chatMessage = ChatMessage.create(chatRoom, mentorId, SenderRole.MENTOR, "테스트 메시지");

            // when
            ChatMessageDelete mentorDelete = ChatMessageDelete.create(chatMessage, mentorId);
            ChatMessageDelete menteeDelete = ChatMessageDelete.create(chatMessage, menteeId);

            // then
            assertThat(mentorDelete.getId().getUserId()).isEqualTo(mentorId);
            assertThat(menteeDelete.getId().getUserId()).isEqualTo(menteeId);
            assertThat(mentorDelete.getChatMessage()).isEqualTo(menteeDelete.getChatMessage());
        }

        @Test
        @DisplayName("여러 메시지에 대해 각각 삭제 기록을 생성할 수 있다")
        void create_multipleMessages() {
            // given
            Long userId = 1L;
            ChatRoom chatRoom = createDefaultChatRoom();
            ChatMessage message1 = ChatMessage.create(chatRoom, 1L, SenderRole.MENTOR, "첫 번째 메시지");
            ChatMessage message2 = ChatMessage.create(chatRoom, 2L, SenderRole.MENTEE, "두 번째 메시지");
            ChatMessage message3 = ChatMessage.create(chatRoom, 1L, SenderRole.MENTOR, "세 번째 메시지");

            // when
            ChatMessageDelete delete1 = ChatMessageDelete.create(message1, userId);
            ChatMessageDelete delete2 = ChatMessageDelete.create(message2, userId);
            ChatMessageDelete delete3 = ChatMessageDelete.create(message3, userId);

            // then
            assertThat(delete1.getChatMessage()).isEqualTo(message1);
            assertThat(delete2.getChatMessage()).isEqualTo(message2);
            assertThat(delete3.getChatMessage()).isEqualTo(message3);
        }
    }

    @Nested
    @DisplayName("ChatMessageDeleteId 복합키 테스트")
    class ChatMessageDeleteIdTest {

        @Test
        @DisplayName("동일한 chatMessageId와 userId를 가진 ChatMessageDeleteId는 동등하다")
        void equals_sameId() {
            // given
            ChatMessageDeleteId id1 = new ChatMessageDeleteId(1L, 2L);
            ChatMessageDeleteId id2 = new ChatMessageDeleteId(1L, 2L);

            // then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 chatMessageId를 가진 ChatMessageDeleteId는 동등하지 않다")
        void equals_differentChatMessageId() {
            // given
            ChatMessageDeleteId id1 = new ChatMessageDeleteId(1L, 2L);
            ChatMessageDeleteId id2 = new ChatMessageDeleteId(3L, 2L);

            // then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("다른 userId를 가진 ChatMessageDeleteId는 동등하지 않다")
        void equals_differentUserId() {
            // given
            ChatMessageDeleteId id1 = new ChatMessageDeleteId(1L, 2L);
            ChatMessageDeleteId id2 = new ChatMessageDeleteId(1L, 3L);

            // then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("ChatMessageDeleteId의 getter가 올바르게 동작한다")
        void getter() {
            // given
            Long chatMessageId = 1L;
            Long userId = 2L;

            // when
            ChatMessageDeleteId id = new ChatMessageDeleteId(chatMessageId, userId);

            // then
            assertThat(id.getChatMessageId()).isEqualTo(chatMessageId);
            assertThat(id.getUserId()).isEqualTo(userId);
        }
    }

    private ChatRoom createDefaultChatRoom() {
        return ChatRoom.create(1L, 2L);
    }
}
