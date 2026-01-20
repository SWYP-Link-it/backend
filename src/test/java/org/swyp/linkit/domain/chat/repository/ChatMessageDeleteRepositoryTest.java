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
import org.swyp.linkit.domain.chat.entity.ChatMessageDelete;
import org.swyp.linkit.domain.chat.entity.ChatMessageDeleteId;
import org.swyp.linkit.domain.chat.entity.ChatRoom;
import org.swyp.linkit.domain.chat.entity.SenderRole;
import org.swyp.linkit.global.config.JpaAuditingConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
@DisplayName("ChatMessageDeleteRepository 테스트")
class ChatMessageDeleteRepositoryTest {

    @Autowired
    private ChatMessageDeleteRepository chatMessageDeleteRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    private ChatRoom chatRoom;
    private ChatMessage message1;
    private ChatMessage message2;
    private ChatMessage message3;
    private Long mentorId;
    private Long menteeId;

    @BeforeEach
    void setUp() {
        mentorId = 1L;
        menteeId = 2L;
        chatRoom = chatRoomRepository.save(ChatRoom.create(mentorId, menteeId));

        message1 = chatMessageRepository.save(
                ChatMessage.create(chatRoom, mentorId, SenderRole.MENTOR, "첫 번째 메시지"));
        message2 = chatMessageRepository.save(
                ChatMessage.create(chatRoom, menteeId, SenderRole.MENTEE, "두 번째 메시지"));
        message3 = chatMessageRepository.save(
                ChatMessage.create(chatRoom, mentorId, SenderRole.MENTOR, "세 번째 메시지"));
    }

    @Nested
    @DisplayName("메시지 삭제 기록 저장")
    class SaveChatMessageDelete {

        @Test
        @DisplayName("메시지 삭제 기록을 저장한다")
        void save() {
            // given
            ChatMessageDelete messageDelete = ChatMessageDelete.create(message1, mentorId);

            // when
            ChatMessageDelete savedDelete = chatMessageDeleteRepository.save(messageDelete);

            // then
            assertThat(savedDelete.getId()).isNotNull();
            assertThat(savedDelete.getId().getChatMessageId()).isEqualTo(message1.getId());
            assertThat(savedDelete.getId().getUserId()).isEqualTo(mentorId);
            assertThat(savedDelete.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("멘티가 메시지를 삭제한 기록을 저장한다")
        void save_mentee() {
            // given
            ChatMessageDelete messageDelete = ChatMessageDelete.create(message1, menteeId);

            // when
            ChatMessageDelete savedDelete = chatMessageDeleteRepository.save(messageDelete);

            // then
            assertThat(savedDelete.getId().getUserId()).isEqualTo(menteeId);
        }
    }

    @Nested
    @DisplayName("메시지 삭제 기록 조회")
    class FindChatMessageDelete {

        @Test
        @DisplayName("복합키로 삭제 기록을 조회한다")
        void findById() {
            // given
            ChatMessageDelete messageDelete = ChatMessageDelete.create(message1, mentorId);
            chatMessageDeleteRepository.save(messageDelete);

            ChatMessageDeleteId id = new ChatMessageDeleteId(message1.getId(), mentorId);

            // when
            Optional<ChatMessageDelete> foundDelete = chatMessageDeleteRepository.findById(id);

            // then
            assertThat(foundDelete).isPresent();
            assertThat(foundDelete.get().getChatMessage().getId()).isEqualTo(message1.getId());
        }

        @Test
        @DisplayName("특정 사용자가 삭제한 메시지 ID 목록을 조회한다")
        void findDeletedMessageIdsByUserId() {
            // given
            chatMessageDeleteRepository.save(ChatMessageDelete.create(message1, mentorId));
            chatMessageDeleteRepository.save(ChatMessageDelete.create(message2, mentorId));
            // message3은 삭제하지 않음

            // when
            List<Long> deletedMessageIds = chatMessageDeleteRepository.findDeletedMessageIdsByUserId(mentorId);

            // then
            assertThat(deletedMessageIds).hasSize(2);
            assertThat(deletedMessageIds).contains(message1.getId(), message2.getId());
            assertThat(deletedMessageIds).doesNotContain(message3.getId());
        }

        @Test
        @DisplayName("삭제한 메시지가 없으면 빈 리스트를 반환한다")
        void findDeletedMessageIdsByUserId_empty() {
            // when
            List<Long> deletedMessageIds = chatMessageDeleteRepository.findDeletedMessageIdsByUserId(999L);

            // then
            assertThat(deletedMessageIds).isEmpty();
        }
    }

    @Nested
    @DisplayName("메시지 삭제 여부 확인")
    class ExistsChatMessageDelete {

        @Test
        @DisplayName("사용자가 메시지를 삭제했는지 확인한다 - true")
        void existsById_ChatMessageIdAndId_UserId_true() {
            // given
            chatMessageDeleteRepository.save(ChatMessageDelete.create(message1, mentorId));

            // when
            boolean exists = chatMessageDeleteRepository.existsById_ChatMessageIdAndId_UserId(
                    message1.getId(), mentorId);

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("사용자가 메시지를 삭제하지 않았으면 false를 반환한다")
        void existsById_ChatMessageIdAndId_UserId_false() {
            // when
            boolean exists = chatMessageDeleteRepository.existsById_ChatMessageIdAndId_UserId(
                    message1.getId(), mentorId);

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("다른 사용자가 삭제해도 본인의 삭제 여부는 false이다")
        void existsById_ChatMessageIdAndId_UserId_otherUserDeleted() {
            // given
            chatMessageDeleteRepository.save(ChatMessageDelete.create(message1, menteeId));

            // when
            boolean exists = chatMessageDeleteRepository.existsById_ChatMessageIdAndId_UserId(
                    message1.getId(), mentorId);

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("다중 사용자 삭제 기록")
    class MultiUserDelete {

        @Test
        @DisplayName("같은 메시지를 멘토와 멘티가 각각 삭제할 수 있다")
        void bothUsersCanDelete() {
            // given
            ChatMessageDelete mentorDelete = ChatMessageDelete.create(message1, mentorId);
            ChatMessageDelete menteeDelete = ChatMessageDelete.create(message1, menteeId);

            chatMessageDeleteRepository.save(mentorDelete);
            chatMessageDeleteRepository.save(menteeDelete);

            // when
            boolean mentorDeleted = chatMessageDeleteRepository.existsById_ChatMessageIdAndId_UserId(
                    message1.getId(), mentorId);
            boolean menteeDeleted = chatMessageDeleteRepository.existsById_ChatMessageIdAndId_UserId(
                    message1.getId(), menteeId);

            // then
            assertThat(mentorDeleted).isTrue();
            assertThat(menteeDeleted).isTrue();
        }

        @Test
        @DisplayName("사용자별 삭제 메시지 목록이 독립적으로 관리된다")
        void independentDeleteLists() {
            // given
            // 멘토는 message1, message2 삭제
            chatMessageDeleteRepository.save(ChatMessageDelete.create(message1, mentorId));
            chatMessageDeleteRepository.save(ChatMessageDelete.create(message2, mentorId));

            // 멘티는 message2, message3 삭제
            chatMessageDeleteRepository.save(ChatMessageDelete.create(message2, menteeId));
            chatMessageDeleteRepository.save(ChatMessageDelete.create(message3, menteeId));

            // when
            List<Long> mentorDeletedIds = chatMessageDeleteRepository.findDeletedMessageIdsByUserId(mentorId);
            List<Long> menteeDeletedIds = chatMessageDeleteRepository.findDeletedMessageIdsByUserId(menteeId);

            // then
            assertThat(mentorDeletedIds).containsExactlyInAnyOrder(message1.getId(), message2.getId());
            assertThat(menteeDeletedIds).containsExactlyInAnyOrder(message2.getId(), message3.getId());
        }
    }
}