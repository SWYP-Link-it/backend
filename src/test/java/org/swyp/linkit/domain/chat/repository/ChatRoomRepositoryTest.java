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
import org.swyp.linkit.domain.chat.entity.ChatRoomStatus;
import org.swyp.linkit.domain.user.entity.OAuthProvider;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.config.JpaAuditingConfig;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
@DisplayName("ChatRoomRepository 테스트")
class ChatRoomRepositoryTest {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    private User mentor;
    private User mentee;

    @BeforeEach
    void setUp() {
        mentor = createAndSaveUser("mentor");
        mentee = createAndSaveUser("mentee");
    }

    @Nested
    @DisplayName("채팅방 생성 및 저장")
    class CreateAndSave {

        @Test
        @DisplayName("채팅방을 저장한다")
        void save() {
            // given
            ChatRoom room = ChatRoom.create(mentor, mentee);

            // when
            ChatRoom savedRoom = chatRoomRepository.save(room);

            // then
            assertThat(savedRoom.getId()).isNotNull();
            assertThat(savedRoom.getMentorId()).isEqualTo(mentor.getId());
            assertThat(savedRoom.getMenteeId()).isEqualTo(mentee.getId());
            assertThat(savedRoom.getStatus()).isEqualTo(ChatRoomStatus.OPEN);
        }

        @Test
        @DisplayName("채팅방 생성 시 기본 상태는 OPEN이다")
        void save_defaultStatus() {
            // given
            ChatRoom room = ChatRoom.create(mentor, mentee);

            // when
            ChatRoom savedRoom = chatRoomRepository.save(room);

            // then
            assertThat(savedRoom.getStatus()).isEqualTo(ChatRoomStatus.OPEN);
        }
    }

    @Nested
    @DisplayName("채팅방 조회")
    class FindChatRoom {

        @Test
        @DisplayName("멘토와 멘티 ID로 채팅방을 조회한다")
        void findByMentorIdAndMenteeId() {
            // given
            ChatRoom room = ChatRoom.create(mentor, mentee);
            chatRoomRepository.save(room);

            // when
            Optional<ChatRoom> foundRoom = chatRoomRepository.findByMentorIdAndMenteeId(mentor.getId(), mentee.getId());

            // then
            assertThat(foundRoom).isPresent();
            assertThat(foundRoom.get().getMentorId()).isEqualTo(mentor.getId());
            assertThat(foundRoom.get().getMenteeId()).isEqualTo(mentee.getId());
        }

        @Test
        @DisplayName("멘토와 멘티 ID가 반대여도 채팅방을 조회한다")
        void findByMentorIdAndMenteeId_reversed() {
            // given
            ChatRoom room = ChatRoom.create(mentor, mentee);
            chatRoomRepository.save(room);

            // when
            Optional<ChatRoom> foundRoom = chatRoomRepository.findByMentorIdAndMenteeId(mentee.getId(), mentor.getId());

            // then
            assertThat(foundRoom).isPresent();
        }

        @Test
        @DisplayName("존재하지 않는 채팅방은 빈 Optional을 반환한다")
        void findByMentorIdAndMenteeId_notFound() {
            // when
            Optional<ChatRoom> foundRoom = chatRoomRepository.findByMentorIdAndMenteeId(999L, 888L);

            // then
            assertThat(foundRoom).isEmpty();
        }

        @Test
        @DisplayName("ID로 채팅방을 조회한다")
        void findById() {
            // given
            ChatRoom room = ChatRoom.create(mentor, mentee);
            ChatRoom savedRoom = chatRoomRepository.save(room);

            // when
            Optional<ChatRoom> foundRoom = chatRoomRepository.findById(savedRoom.getId());

            // then
            assertThat(foundRoom).isPresent();
            assertThat(foundRoom.get().getId()).isEqualTo(savedRoom.getId());
        }
    }

    @Nested
    @DisplayName("참여자 확인 (existsByIdAndUserId)")
    class ParticipantCheck {

        @Test
        @DisplayName("멘토가 참여자인지 확인한다 - true 반환")
        void existsByIdAndUserId_mentor_returnsTrue() {
            // given
            ChatRoom room = ChatRoom.create(mentor, mentee);
            ChatRoom savedRoom = chatRoomRepository.save(room);

            // when
            boolean isParticipant = chatRoomRepository.existsByIdAndUserId(savedRoom.getId(), mentor.getId());

            // then
            assertThat(isParticipant).isTrue();
        }

        @Test
        @DisplayName("멘티가 참여자인지 확인한다 - true 반환")
        void existsByIdAndUserId_mentee_returnsTrue() {
            // given
            ChatRoom room = ChatRoom.create(mentor, mentee);
            ChatRoom savedRoom = chatRoomRepository.save(room);

            // when
            boolean isParticipant = chatRoomRepository.existsByIdAndUserId(savedRoom.getId(), mentee.getId());

            // then
            assertThat(isParticipant).isTrue();
        }

        @Test
        @DisplayName("참여자가 아닌 경우 false를 반환한다")
        void existsByIdAndUserId_nonParticipant_returnsFalse() {
            // given
            ChatRoom room = ChatRoom.create(mentor, mentee);
            ChatRoom savedRoom = chatRoomRepository.save(room);
            Long nonParticipantId = 999L;

            // when
            boolean isParticipant = chatRoomRepository.existsByIdAndUserId(savedRoom.getId(), nonParticipantId);

            // then
            assertThat(isParticipant).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 채팅방 ID는 false를 반환한다")
        void existsByIdAndUserId_roomNotFound_returnsFalse() {
            // when
            boolean isParticipant = chatRoomRepository.existsByIdAndUserId(999L, mentor.getId());

            // then
            assertThat(isParticipant).isFalse();
        }
    }

    private User createAndSaveUser(String suffix) {
        return userRepository.save(
                User.create(OAuthProvider.KAKAO, "oauth_" + suffix, suffix + "@test.com",
                        "Test " + suffix, null, suffix)
        );
    }
}
