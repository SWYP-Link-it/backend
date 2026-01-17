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
import org.swyp.linkit.domain.user.entity.UserStatus;
import org.swyp.linkit.global.config.JpaAuditingConfig;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자를 저장한다")
    void save() {
        // given
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_12345",
                "test@example.com",
                "홍길동",
                "테스터"
        );

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getOauthProvider()).isEqualTo(OAuthProvider.KAKAO);
        assertThat(savedUser.getOauthId()).isEqualTo("kakao_12345");
        assertThat(savedUser.getUserStatus()).isEqualTo(UserStatus.PROFILE_PENDING);
    }

    @Test
    @DisplayName("OAuth 제공자와 OAuth ID로 사용자를 조회한다")
    void findByOauthProviderAndOauthId() {
        // given
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_12345",
                "test@example.com",
                "홍길동",
                "테스터"
        );
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByOauthProviderAndOauthId(
                OAuthProvider.KAKAO,
                "kakao_12345"
        );

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 OAuth 정보로 조회 시 빈 Optional을 반환한다")
    void findByOauthProviderAndOauthId_notFound() {
        // when
        Optional<User> foundUser = userRepository.findByOauthProviderAndOauthId(
                OAuthProvider.KAKAO,
                "not_exist"
        );

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("탈퇴하지 않은 사용자만 조회한다")
    void findByOauthProviderAndOauthIdAndUserStatusNot() {
        // given
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_12345",
                "test@example.com",
                "홍길동",
                "테스터"
        );
        user.withdraw();
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByOauthProviderAndOauthIdAndUserStatusNot(
                OAuthProvider.KAKAO,
                "kakao_12345",
                UserStatus.WITHDRAWN
        );

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("활성 사용자는 정상 조회된다")
    void findByOauthProviderAndOauthIdAndUserStatusNot_active() {
        // given
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_12345",
                "test@example.com",
                "홍길동",
                "테스터"
        );
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByOauthProviderAndOauthIdAndUserStatusNot(
                OAuthProvider.KAKAO,
                "kakao_12345",
                UserStatus.WITHDRAWN
        );

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserStatus()).isEqualTo(UserStatus.PROFILE_PENDING);
    }

    @Test
    @DisplayName("닉네임으로 사용자를 조회한다")
    void findByNickname() {
        // given
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_12345",
                "test@example.com",
                "홍길동",
                "테스터123"
        );
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByNickname("테스터123");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("닉네임 존재 여부를 확인한다")
    void existsByNickname() {
        // given
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_12345",
                "test@example.com",
                "홍길동",
                "테스터123"
        );
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByNickname("테스터123");
        boolean notExists = userRepository.existsByNickname("존재안함");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("사용자 ID로 프로필을 포함하여 조회한다")
    void findByIdWithProfile() {
        // given
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_12345",
                "test@example.com",
                "홍길동",
                "테스터"
        );
        UserProfile profile = UserProfile.create(
                user,
                "https://example.com/image.jpg",
                "안녕하세요",
                "경험 설명",
                5,
                true
        );
        user.assignProfile(profile);
        User savedUser = userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByIdWithProfile(savedUser.getId());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserProfile()).isNotNull();
        assertThat(foundUser.get().getUserProfile().getIntroduction()).isEqualTo("안녕하세요");
    }

    @Test
    @DisplayName("프로필이 없는 사용자도 조회된다")
    void findByIdWithProfile_noProfile() {
        // given
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_12345",
                "test@example.com",
                "홍길동",
                "테스터"
        );
        User savedUser = userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByIdWithProfile(savedUser.getId());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserProfile()).isNull();
    }

    @Test
    @DisplayName("OAuth 제공자와 OAuth ID로 프로필을 포함하여 조회한다")
    void findByOauthProviderAndOauthIdWithProfile() {
        // given
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_12345",
                "test@example.com",
                "홍길동",
                "테스터"
        );
        UserProfile profile = UserProfile.create(
                user,
                "https://example.com/image.jpg",
                "안녕하세요",
                "경험 설명",
                5,
                true
        );
        user.assignProfile(profile);
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByOauthProviderAndOauthIdWithProfile(
                OAuthProvider.KAKAO,
                "kakao_12345"
        );

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserProfile()).isNotNull();
        assertThat(foundUser.get().getUserProfile().getIntroduction()).isEqualTo("안녕하세요");
    }
}