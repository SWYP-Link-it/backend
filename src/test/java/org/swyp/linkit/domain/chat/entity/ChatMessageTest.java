package org.swyp.linkit.domain.chat.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatMessage 엔티티 테스트")
class ChatMessageTest {

    @Nested
    @DisplayName("메시지 생성")
    class CreateMessage {

        @Test
        @DisplayName("멘토가 보낸 메시지를 생성한다")
        void create_mentor() {
            // given
            ChatRoom chatRoom = createDefaultChatRoom();
            Long senderId = 1L;
            String content = "안녕하세요, 멘토입니다.";

            // when
            ChatMessage message = ChatMessage.create(chatRoom, senderId, SenderRole.MENTOR, content);

            // then
            assertThat(message.getChatRoom()).isEqualTo(chatRoom);
            assertThat(message.getSenderId()).isEqualTo(senderId);
            assertThat(message.getSenderRole()).isEqualTo(SenderRole.MENTOR);
            assertThat(message.getContent()).isEqualTo(content);
        }

        @Test
        @DisplayName("멘티가 보낸 메시지를 생성한다")
        void create_mentee() {
            // given
            ChatRoom chatRoom = createDefaultChatRoom();
            Long senderId = 2L;
            String content = "안녕하세요, 멘티입니다.";

            // when
            ChatMessage message = ChatMessage.create(chatRoom, senderId, SenderRole.MENTEE, content);

            // then
            assertThat(message.getChatRoom()).isEqualTo(chatRoom);
            assertThat(message.getSenderId()).isEqualTo(senderId);
            assertThat(message.getSenderRole()).isEqualTo(SenderRole.MENTEE);
            assertThat(message.getContent()).isEqualTo(content);
        }

        @Test
        @DisplayName("긴 내용의 메시지를 생성한다")
        void create_longContent() {
            // given
            ChatRoom chatRoom = createDefaultChatRoom();
            Long senderId = 1L;
            String longContent = "가".repeat(1000);

            // when
            ChatMessage message = ChatMessage.create(chatRoom, senderId, SenderRole.MENTOR, longContent);

            // then
            assertThat(message.getContent()).isEqualTo(longContent);
            assertThat(message.getContent()).hasSize(1000);
        }

        @Test
        @DisplayName("이모지가 포함된 메시지를 생성한다")
        void create_withEmoji() {
            // given
            ChatRoom chatRoom = createDefaultChatRoom();
            Long senderId = 1L;
            String contentWithEmoji = "안녕하세요! 😊🎉";

            // when
            ChatMessage message = ChatMessage.create(chatRoom, senderId, SenderRole.MENTOR, contentWithEmoji);

            // then
            assertThat(message.getContent()).isEqualTo(contentWithEmoji);
        }

        @Test
        @DisplayName("빈 내용의 메시지도 생성할 수 있다")
        void create_emptyContent() {
            // given
            ChatRoom chatRoom = createDefaultChatRoom();
            Long senderId = 1L;
            String emptyContent = "";

            // when
            ChatMessage message = ChatMessage.create(chatRoom, senderId, SenderRole.MENTOR, emptyContent);

            // then
            assertThat(message.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("SenderRole 검증")
    class SenderRoleTest {

        @Test
        @DisplayName("MENTOR 역할을 가진 메시지")
        void senderRole_mentor() {
            // given
            ChatRoom chatRoom = createDefaultChatRoom();

            // when
            ChatMessage message = ChatMessage.create(chatRoom, 1L, SenderRole.MENTOR, "테스트");

            // then
            assertThat(message.getSenderRole()).isEqualTo(SenderRole.MENTOR);
            assertThat(message.getSenderRole().name()).isEqualTo("MENTOR");
        }

        @Test
        @DisplayName("MENTEE 역할을 가진 메시지")
        void senderRole_mentee() {
            // given
            ChatRoom chatRoom = createDefaultChatRoom();

            // when
            ChatMessage message = ChatMessage.create(chatRoom, 2L, SenderRole.MENTEE, "테스트");

            // then
            assertThat(message.getSenderRole()).isEqualTo(SenderRole.MENTEE);
            assertThat(message.getSenderRole().name()).isEqualTo("MENTEE");
        }
    }

    private ChatRoom createDefaultChatRoom() {
        return ChatRoom.create(1L, 2L);
    }
}