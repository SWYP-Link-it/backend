package org.swyp.linkit.domain.user.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.swyp.linkit.domain.user.entity.OAuthProvider;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserProfile;
import org.swyp.linkit.global.config.JpaAuditingConfig;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
@DisplayName("UserProfileRepository 테스트")
class UserProfileRepositoryTest {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("프로필을 저장한다")
    void save() {
        // given
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_12345",
                "test@example.com",
                "홍길동",
                "테스터"
        );
        userRepository.save(user);

        UserProfile profile = UserProfile.create(
                user,
                "https://example.com/image.jpg",
                "안녕하세요",
                "경험 설명",
                5,
                true
        );

        // when
        UserProfile savedProfile = userProfileRepository.save(profile);

        // then
        assertThat(savedProfile.getId()).isNotNull();
        assertThat(savedProfile.getUser()).isEqualTo(user);
        assertThat(savedProfile.getIntroduction()).isEqualTo("안녕하세요");
    }

    @Test
    @DisplayName("사용자 ID로 프로필을 조회한다")
    void findByUserId() {
        // given
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_12345",
                "test@example.com",
                "홍길동",
                "테스터"
        );
        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.create(
                savedUser,
                "https://example.com/image.jpg",
                "안녕하세요",
                "경험 설명",
                5,
                true
        );
        userProfileRepository.save(profile);

        // when
        Optional<UserProfile> foundProfile = userProfileRepository.findByUserId(savedUser.getId());

        // then
        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getIntroduction()).isEqualTo("안녕하세요");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 조회 시 빈 Optional을 반환한다")
    void findByUserId_notFound() {
        // when
        Optional<UserProfile> foundProfile = userProfileRepository.findByUserId(999L);

        // then
        assertThat(foundProfile).isEmpty();
    }

    @Test
    @DisplayName("사용자 ID로 프로필 존재 여부를 확인한다")
    void existsByUserId() {
        // given
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_12345",
                "test@example.com",
                "홍길동",
                "테스터"
        );
        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.create(
                savedUser,
                "https://example.com/image.jpg",
                "안녕하세요",
                "경험 설명",
                5,
                true
        );
        userProfileRepository.save(profile);

        // when
        boolean exists = userProfileRepository.existsByUserId(savedUser.getId());
        boolean notExists = userProfileRepository.existsByUserId(999L);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("사용자 ID로 프로필을 삭제한다")
    void deleteByUserId() {
        // given
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_12345",
                "test@example.com",
                "홍길동",
                "테스터"
        );
        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.create(
                savedUser,
                "https://example.com/image.jpg",
                "안녕하세요",
                "경험 설명",
                5,
                true
        );
        userProfileRepository.save(profile);

        // when
        userProfileRepository.deleteByUserId(savedUser.getId());

        // then
        Optional<UserProfile> foundProfile = userProfileRepository.findByUserId(savedUser.getId());
        assertThat(foundProfile).isEmpty();
    }

    @Test
    @DisplayName("사용자 ID로 프로필과 사용자를 함께 조회한다")
    void findByUserIdWithUser() {
        // given
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_12345",
                "test@example.com",
                "홍길동",
                "테스터"
        );
        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.create(
                savedUser,
                "https://example.com/image.jpg",
                "안녕하세요",
                "경험 설명",
                5,
                true
        );
        userProfileRepository.save(profile);

        // when
        Optional<UserProfile> foundProfile = userProfileRepository.findByUserIdWithUser(savedUser.getId());

        // then
        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getUser()).isNotNull();
        assertThat(foundProfile.get().getUser().getName()).isEqualTo("홍길동");
    }
}