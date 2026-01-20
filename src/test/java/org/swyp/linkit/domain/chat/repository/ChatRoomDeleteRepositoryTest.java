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
import org.swyp.linkit.domain.chat.entity.ChatRoom;
import org.swyp.linkit.domain.chat.entity.ChatRoomDelete;
import org.swyp.linkit.domain.chat.entity.ChatRoomDeleteId;
import org.swyp.linkit.global.config.JpaAuditingConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
@DisplayName("ChatRoomDeleteRepository 테스트")
class ChatRoomDeleteRepositoryTest {

    @Autowired
    private ChatRoomDeleteRepository chatRoomDeleteRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    private ChatRoom chatRoom1;
    private ChatRoom chatRoom2;
    private ChatRoom chatRoom3;
    private Long mentorId;
    private Long menteeId;

    @BeforeEach
    void setUp() {
        mentorId = 1L;
        menteeId = 2L;
        chatRoom1 = chatRoomRepository.save(ChatRoom.create(mentorId, menteeId));
        chatRoom2 = chatRoomRepository.save(ChatRoom.create(mentorId, 3L));
        chatRoom3 = chatRoomRepository.save(ChatRoom.create(mentorId, 4L));
    }

    @Nested
    @DisplayName("채팅방 삭제 기록 저장")
    class SaveChatRoomDelete {

        @Test
        @DisplayName("채팅방 삭제 기록을 저장한다")
        void save() {
            // given
            ChatRoomDelete chatRoomDelete = ChatRoomDelete.create(chatRoom1, mentorId);

            // when
            ChatRoomDelete savedDelete = chatRoomDeleteRepository.save(chatRoomDelete);

            // then
            assertThat(savedDelete.getId()).isNotNull();
            assertThat(savedDelete.getId().getChatRoomId()).isEqualTo(chatRoom1.getId());
            assertThat(savedDelete.getId().getUserId()).isEqualTo(mentorId);
            assertThat(savedDelete.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("멘티가 채팅방을 삭제한 기록을 저장한다")
        void save_mentee() {
            // given
            ChatRoomDelete chatRoomDelete = ChatRoomDelete.create(chatRoom1, menteeId);

            // when
            ChatRoomDelete savedDelete = chatRoomDeleteRepository.save(chatRoomDelete);

            // then
            assertThat(savedDelete.getId().getUserId()).isEqualTo(menteeId);
        }
    }

    @Nested
    @DisplayName("채팅방 삭제 기록 조회")
    class FindChatRoomDelete {

        @Test
        @DisplayName("복합키로 삭제 기록을 조회한다")
        void findById() {
            // given
            ChatRoomDelete chatRoomDelete = ChatRoomDelete.create(chatRoom1, mentorId);
            chatRoomDeleteRepository.save(chatRoomDelete);

            ChatRoomDeleteId id = new ChatRoomDeleteId(chatRoom1.getId(), mentorId);

            // when
            Optional<ChatRoomDelete> foundDelete = chatRoomDeleteRepository.findById(id);

            // then
            assertThat(foundDelete).isPresent();
            assertThat(foundDelete.get().getChatRoom().getId()).isEqualTo(chatRoom1.getId());
        }

        @Test
        @DisplayName("특정 사용자가 삭제한 채팅방 ID 목록을 조회한다")
        void findDeletedRoomIdsByUserId() {
            // given
            chatRoomDeleteRepository.save(ChatRoomDelete.create(chatRoom1, mentorId));
            chatRoomDeleteRepository.save(ChatRoomDelete.create(chatRoom2, mentorId));
            // chatRoom3은 삭제하지 않음

            // when
            List<Long> deletedRoomIds = chatRoomDeleteRepository.findDeletedRoomIdsByUserId(mentorId);

            // then
            assertThat(deletedRoomIds).hasSize(2);
            assertThat(deletedRoomIds).contains(chatRoom1.getId(), chatRoom2.getId());
            assertThat(deletedRoomIds).doesNotContain(chatRoom3.getId());
        }

        @Test
        @DisplayName("삭제한 채팅방이 없으면 빈 리스트를 반환한다")
        void findDeletedRoomIdsByUserId_empty() {
            // when
            List<Long> deletedRoomIds = chatRoomDeleteRepository.findDeletedRoomIdsByUserId(999L);

            // then
            assertThat(deletedRoomIds).isEmpty();
        }
    }

    @Nested
    @DisplayName("채팅방 삭제 여부 확인")
    class ExistsChatRoomDelete {

        @Test
        @DisplayName("사용자가 채팅방을 삭제했는지 확인한다 - true")
        void existsById_ChatRoomIdAndId_UserId_true() {
            // given
            chatRoomDeleteRepository.save(ChatRoomDelete.create(chatRoom1, mentorId));

            // when
            boolean exists = chatRoomDeleteRepository.existsById_ChatRoomIdAndId_UserId(
                    chatRoom1.getId(), mentorId);

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("사용자가 채팅방을 삭제하지 않았으면 false를 반환한다")
        void existsById_ChatRoomIdAndId_UserId_false() {
            // when
            boolean exists = chatRoomDeleteRepository.existsById_ChatRoomIdAndId_UserId(
                    chatRoom1.getId(), mentorId);

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("다른 사용자가 삭제해도 본인의 삭제 여부는 false이다")
        void existsById_ChatRoomIdAndId_UserId_otherUserDeleted() {
            // given
            chatRoomDeleteRepository.save(ChatRoomDelete.create(chatRoom1, menteeId));

            // when
            boolean exists = chatRoomDeleteRepository.existsById_ChatRoomIdAndId_UserId(
                    chatRoom1.getId(), mentorId);

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("다중 사용자 삭제 기록")
    class MultiUserDelete {

        @Test
        @DisplayName("같은 채팅방을 멘토와 멘티가 각각 삭제할 수 있다")
        void bothUsersCanDelete() {
            // given
            ChatRoomDelete mentorDelete = ChatRoomDelete.create(chatRoom1, mentorId);
            ChatRoomDelete menteeDelete = ChatRoomDelete.create(chatRoom1, menteeId);

            chatRoomDeleteRepository.save(mentorDelete);
            chatRoomDeleteRepository.save(menteeDelete);

            // when
            boolean mentorDeleted = chatRoomDeleteRepository.existsById_ChatRoomIdAndId_UserId(
                    chatRoom1.getId(), mentorId);
            boolean menteeDeleted = chatRoomDeleteRepository.existsById_ChatRoomIdAndId_UserId(
                    chatRoom1.getId(), menteeId);

            // then
            assertThat(mentorDeleted).isTrue();
            assertThat(menteeDeleted).isTrue();
        }
    }
}