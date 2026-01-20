package org.swyp.linkit.domain.chat.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.swyp.linkit.domain.chat.entity.ChatMessage;
import org.swyp.linkit.domain.chat.entity.ChatRoom;
import org.swyp.linkit.domain.chat.entity.SenderRole;
import org.swyp.linkit.global.config.JpaAuditingConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
@DisplayName("ChatMessageRepository 테스트")
class ChatMessageRepositoryTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    private ChatRoom chatRoom;
    private Long mentorId;
    private Long menteeId;

    @BeforeEach
    void setUp() {
        mentorId = 1L;
        menteeId = 2L;
        chatRoom = chatRoomRepository.save(ChatRoom.create(mentorId, menteeId));
    }

    @Nested
    @DisplayName("메시지 저장")
    class SaveMessage {

        @Test
        @DisplayName("메시지를 저장한다")
        void save() {
            // given
            ChatMessage message = ChatMessage.create(chatRoom, mentorId, SenderRole.MENTOR, "안녕하세요");

            // when
            ChatMessage savedMessage = chatMessageRepository.save(message);

            // then
            assertThat(savedMessage.getId()).isNotNull();
            assertThat(savedMessage.getChatRoom().getId()).isEqualTo(chatRoom.getId());
            assertThat(savedMessage.getSenderId()).isEqualTo(mentorId);
            assertThat(savedMessage.getSenderRole()).isEqualTo(SenderRole.MENTOR);
            assertThat(savedMessage.getContent()).isEqualTo("안녕하세요");
        }

        @Test
        @DisplayName("멘티가 보낸 메시지를 저장한다")
        void save_menteeMessage() {
            // given
            ChatMessage message = ChatMessage.create(chatRoom, menteeId, SenderRole.MENTEE, "반갑습니다");

            // when
            ChatMessage savedMessage = chatMessageRepository.save(message);

            // then
            assertThat(savedMessage.getSenderId()).isEqualTo(menteeId);
            assertThat(savedMessage.getSenderRole()).isEqualTo(SenderRole.MENTEE);
        }
    }

    @Nested
    @DisplayName("메시지 조회")
    class FindMessages {

        @Test
        @DisplayName("채팅방의 모든 메시지를 생성순으로 조회한다")
        void findByChatRoomIdOrderByCreatedAtAsc() {
            // given
            ChatMessage message1 = chatMessageRepository.save(
                    ChatMessage.create(chatRoom, mentorId, SenderRole.MENTOR, "첫 번째 메시지"));
            ChatMessage message2 = chatMessageRepository.save(
                    ChatMessage.create(chatRoom, menteeId, SenderRole.MENTEE, "두 번째 메시지"));
            ChatMessage message3 = chatMessageRepository.save(
                    ChatMessage.create(chatRoom, mentorId, SenderRole.MENTOR, "세 번째 메시지"));

            // when
            List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoom.getId());

            // then
            assertThat(messages).hasSize(3);
            assertThat(messages.get(0).getContent()).isEqualTo("첫 번째 메시지");
            assertThat(messages.get(1).getContent()).isEqualTo("두 번째 메시지");
            assertThat(messages.get(2).getContent()).isEqualTo("세 번째 메시지");
        }

        @Test
        @DisplayName("채팅방의 최근 메시지 50개를 조회한다")
        void findTop50ByChatRoomIdOrderByIdDesc() {
            // given
            for (int i = 1; i <= 60; i++) {
                chatMessageRepository.save(
                        ChatMessage.create(chatRoom, mentorId, SenderRole.MENTOR, "메시지 " + i));
            }

            // when
            List<ChatMessage> messages = chatMessageRepository.findTop50ByChatRoomIdOrderByIdDesc(chatRoom.getId());

            // then
            assertThat(messages).hasSize(50);
            // 최신순이므로 첫 번째 메시지가 가장 최근 메시지
            assertThat(messages.get(0).getContent()).isEqualTo("메시지 60");
        }

        @Test
        @DisplayName("특정 메시지 ID 이후의 메시지를 조회한다")
        void findByChatRoomIdAndIdGreaterThan() {
            // given
            ChatMessage message1 = chatMessageRepository.save(
                    ChatMessage.create(chatRoom, mentorId, SenderRole.MENTOR, "첫 번째 메시지"));
            ChatMessage message2 = chatMessageRepository.save(
                    ChatMessage.create(chatRoom, menteeId, SenderRole.MENTEE, "두 번째 메시지"));
            ChatMessage message3 = chatMessageRepository.save(
                    ChatMessage.create(chatRoom, mentorId, SenderRole.MENTOR, "세 번째 메시지"));

            // when
            List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndIdGreaterThan(
                    chatRoom.getId(), message1.getId());

            // then
            assertThat(messages).hasSize(2);
            assertThat(messages.get(0).getContent()).isEqualTo("두 번째 메시지");
            assertThat(messages.get(1).getContent()).isEqualTo("세 번째 메시지");
        }

        @Test
        @DisplayName("채팅방의 마지막 메시지를 조회한다")
        void findLastMessageByChatRoomId() {
            // given
            chatMessageRepository.save(
                    ChatMessage.create(chatRoom, mentorId, SenderRole.MENTOR, "첫 번째 메시지"));
            chatMessageRepository.save(
                    ChatMessage.create(chatRoom, menteeId, SenderRole.MENTEE, "두 번째 메시지"));
            chatMessageRepository.save(
                    ChatMessage.create(chatRoom, mentorId, SenderRole.MENTOR, "마지막 메시지"));

            // when
            ChatMessage lastMessage = chatMessageRepository.findLastMessageByChatRoomId(chatRoom.getId());

            // then
            assertThat(lastMessage).isNotNull();
            assertThat(lastMessage.getContent()).isEqualTo("마지막 메시지");
        }

        @Test
        @DisplayName("메시지가 없는 채팅방의 마지막 메시지는 null이다")
        void findLastMessageByChatRoomId_empty() {
            // when
            ChatMessage lastMessage = chatMessageRepository.findLastMessageByChatRoomId(chatRoom.getId());

            // then
            assertThat(lastMessage).isNull();
        }
    }

    @Nested
    @DisplayName("메시지 수 조회")
    class CountMessages {

        @Test
        @DisplayName("특정 메시지 ID 이후의 메시지 개수를 조회한다")
        void countByChatRoomIdAndIdGreaterThan() {
            // given
            ChatMessage message1 = chatMessageRepository.save(
                    ChatMessage.create(chatRoom, mentorId, SenderRole.MENTOR, "첫 번째 메시지"));
            chatMessageRepository.save(
                    ChatMessage.create(chatRoom, menteeId, SenderRole.MENTEE, "두 번째 메시지"));
            chatMessageRepository.save(
                    ChatMessage.create(chatRoom, mentorId, SenderRole.MENTOR, "세 번째 메시지"));
            chatMessageRepository.save(
                    ChatMessage.create(chatRoom, menteeId, SenderRole.MENTEE, "네 번째 메시지"));

            // when
            long count = chatMessageRepository.countByChatRoomIdAndIdGreaterThan(
                    chatRoom.getId(), message1.getId());

            // then
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("이후 메시지가 없으면 0을 반환한다")
        void countByChatRoomIdAndIdGreaterThan_noMessages() {
            // given
            ChatMessage lastMessage = chatMessageRepository.save(
                    ChatMessage.create(chatRoom, mentorId, SenderRole.MENTOR, "마지막 메시지"));

            // when
            long count = chatMessageRepository.countByChatRoomIdAndIdGreaterThan(
                    chatRoom.getId(), lastMessage.getId());

            // then
            assertThat(count).isZero();
        }
    }
}