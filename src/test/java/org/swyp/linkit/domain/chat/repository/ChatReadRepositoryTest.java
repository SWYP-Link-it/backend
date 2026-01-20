package org.swyp.linkit.domain.chat.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.swyp.linkit.domain.chat.entity.ChatRead;
import org.swyp.linkit.domain.chat.entity.ChatReadId;
import org.swyp.linkit.domain.chat.entity.ChatRoom;
import org.swyp.linkit.global.config.JpaAuditingConfig;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
@DisplayName("ChatReadRepository 테스트")
class ChatReadRepositoryTest {

    @Autowired
    private ChatReadRepository chatReadRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private EntityManager entityManager;

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
    @DisplayName("읽음 정보 저장")
    class SaveChatRead {

        @Test
        @DisplayName("읽음 정보를 저장한다")
        void save() {
            // given
            Long lastReadMessageId = 100L;
            ChatRead chatRead = ChatRead.create(chatRoom, mentorId, lastReadMessageId);

            // when
            ChatRead savedChatRead = chatReadRepository.save(chatRead);

            // then
            assertThat(savedChatRead.getId()).isNotNull();
            assertThat(savedChatRead.getId().getChatRoomId()).isEqualTo(chatRoom.getId());
            assertThat(savedChatRead.getId().getUserId()).isEqualTo(mentorId);
            assertThat(savedChatRead.getLastReadMessageId()).isEqualTo(lastReadMessageId);
        }

        @Test
        @DisplayName("멘티의 읽음 정보를 저장한다")
        void save_mentee() {
            // given
            Long lastReadMessageId = 50L;
            ChatRead chatRead = ChatRead.create(chatRoom, menteeId, lastReadMessageId);

            // when
            ChatRead savedChatRead = chatReadRepository.save(chatRead);

            // then
            assertThat(savedChatRead.getId().getUserId()).isEqualTo(menteeId);
            assertThat(savedChatRead.getLastReadMessageId()).isEqualTo(lastReadMessageId);
        }
    }

    @Nested
    @DisplayName("읽음 정보 조회")
    class FindChatRead {

        @Test
        @DisplayName("복합키로 읽음 정보를 조회한다")
        void findById() {
            // given
            Long lastReadMessageId = 100L;
            ChatRead chatRead = ChatRead.create(chatRoom, mentorId, lastReadMessageId);
            chatReadRepository.save(chatRead);

            ChatReadId id = new ChatReadId(chatRoom.getId(), mentorId);

            // when
            Optional<ChatRead> foundChatRead = chatReadRepository.findById(id);

            // then
            assertThat(foundChatRead).isPresent();
            assertThat(foundChatRead.get().getLastReadMessageId()).isEqualTo(lastReadMessageId);
        }

        @Test
        @DisplayName("채팅방 ID와 사용자 ID로 읽음 정보를 조회한다")
        void findById_ChatRoomIdAndId_UserId() {
            // given
            Long lastReadMessageId = 100L;
            ChatRead chatRead = ChatRead.create(chatRoom, mentorId, lastReadMessageId);
            chatReadRepository.save(chatRead);

            // when
            Optional<ChatRead> foundChatRead = chatReadRepository.findById_ChatRoomIdAndId_UserId(
                    chatRoom.getId(), mentorId);

            // then
            assertThat(foundChatRead).isPresent();
            assertThat(foundChatRead.get().getLastReadMessageId()).isEqualTo(lastReadMessageId);
        }

        @Test
        @DisplayName("존재하지 않는 읽음 정보는 빈 Optional을 반환한다")
        void findById_ChatRoomIdAndId_UserId_notFound() {
            // when
            Optional<ChatRead> foundChatRead = chatReadRepository.findById_ChatRoomIdAndId_UserId(
                    chatRoom.getId(), 999L);

            // then
            assertThat(foundChatRead).isEmpty();
        }
    }

    @Nested
    @DisplayName("읽음 정보 업데이트")
    class UpdateChatRead {

        @Test
        @DisplayName("마지막 읽은 메시지를 업데이트한다")
        void updateLastReadMessage() {
            // given
            Long initialMessageId = 100L;
            Long newMessageId = 200L;
            ChatRead chatRead = ChatRead.create(chatRoom, mentorId, initialMessageId);
            ChatRead savedChatRead = chatReadRepository.save(chatRead);

            // when
            savedChatRead.updateLastReadMessage(newMessageId);
            chatReadRepository.flush();
            entityManager.clear();

            // then
            Optional<ChatRead> updatedChatRead = chatReadRepository.findById_ChatRoomIdAndId_UserId(
                    chatRoom.getId(), mentorId);
            assertThat(updatedChatRead).isPresent();
            assertThat(updatedChatRead.get().getLastReadMessageId()).isEqualTo(newMessageId);
        }
    }

    @Nested
    @DisplayName("다중 사용자 읽음 정보")
    class MultiUserChatRead {

        @Test
        @DisplayName("같은 채팅방에서 멘토와 멘티의 읽음 정보가 독립적으로 관리된다")
        void independentReadInfo() {
            // given
            Long mentorLastRead = 100L;
            Long menteeLastRead = 50L;

            ChatRead mentorRead = ChatRead.create(chatRoom, mentorId, mentorLastRead);
            ChatRead menteeRead = ChatRead.create(chatRoom, menteeId, menteeLastRead);

            chatReadRepository.save(mentorRead);
            chatReadRepository.save(menteeRead);

            // when
            Optional<ChatRead> foundMentorRead = chatReadRepository.findById_ChatRoomIdAndId_UserId(
                    chatRoom.getId(), mentorId);
            Optional<ChatRead> foundMenteeRead = chatReadRepository.findById_ChatRoomIdAndId_UserId(
                    chatRoom.getId(), menteeId);

            // then
            assertThat(foundMentorRead).isPresent();
            assertThat(foundMenteeRead).isPresent();
            assertThat(foundMentorRead.get().getLastReadMessageId()).isEqualTo(mentorLastRead);
            assertThat(foundMenteeRead.get().getLastReadMessageId()).isEqualTo(menteeLastRead);
        }
    }
}