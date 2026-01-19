package org.swyp.linkit.domain.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("User 엔티티 테스트")
class UserTest {

    @Test
    @DisplayName("OAuth 소셜 로그인으로 신규 사용자를 생성한다")
    void createUser() {
        // given
        OAuthProvider provider = OAuthProvider.KAKAO;
        String oauthId = "kakao_12345";
        String email = "test@example.com";
        String name = "홍길동";
        String nickname = "테스터";

        // when
        User user = User.create(provider, oauthId, email, name, nickname);

        // then
        assertThat(user.getOauthProvider()).isEqualTo(provider);
        assertThat(user.getOauthId()).isEqualTo(oauthId);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getUserStatus()).isEqualTo(UserStatus.PROFILE_PENDING);
        assertThat(user.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("프로필 작성 완료 시 상태가 PROFILE_PENDING에서 ACTIVE로 변경된다")
    void completeProfile() {
        // given
        User user = createDefaultUser();

        // when
        user.completeProfile();

        // then
        assertThat(user.getUserStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("이미 ACTIVE 상태인 사용자가 completeProfile을 호출해도 상태가 유지된다")
    void completeProfile_alreadyActive() {
        // given
        User user = createDefaultUser();
        user.completeProfile();

        // when
        user.completeProfile();

        // then
        assertThat(user.getUserStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("프로필을 삭제하면 연관관계가 해제된다")
    void removeProfile() {
        // given
        User user = createDefaultUser();
        UserProfile profile = createDefaultProfile(user);
        user.assignProfile(profile);

        // when
        user.removeProfile();

        // then
        assertThat(user.getUserProfile()).isNull();
        assertThat(profile.getUser()).isNull();
    }

    @Test
    @DisplayName("프로필이 없는 상태에서 removeProfile을 호출해도 예외가 발생하지 않는다")
    void removeProfile_noProfile() {
        // given
        User user = createDefaultUser();

        // when & then
        assertThatCode(user::removeProfile)
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("프로필 연관관계를 설정하면 양방향으로 연결된다")
    void assignProfile() {
        // given
        User user = createDefaultUser();
        UserProfile profile = createDefaultProfile(user);

        // when
        user.assignProfile(profile);

        // then
        assertThat(user.getUserProfile()).isEqualTo(profile);
        assertThat(profile.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("회원 탈퇴 시 상태가 WITHDRAWN으로 변경되고 탈퇴 시각이 기록된다")
    void withdraw() {
        // given
        User user = createDefaultUser();
        LocalDateTime beforeWithdraw = LocalDateTime.now();

        // when
        user.withdraw();

        // then
        LocalDateTime afterWithdraw = LocalDateTime.now();
        assertThat(user.getUserStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.getDeletedAt()).isBetween(beforeWithdraw, afterWithdraw);
        assertThat(user.getUserProfile()).isNull();
    }

    @Test
    @DisplayName("프로필이 있는 사용자가 탈퇴하면 프로필도 함께 제거된다")
    void withdraw_withProfile() {
        // given
        User user = createDefaultUser();
        UserProfile profile = createDefaultProfile(user);
        user.assignProfile(profile);

        // when
        user.withdraw();

        // then
        assertThat(user.getUserStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(user.getUserProfile()).isNull();
        assertThat(profile.getUser()).isNull();
    }

    @Test
    @DisplayName("PROFILE_PENDING 상태일 때 프로필 최초 작성 여부가 true를 반환한다")
    void isFirstProfileCompletion_pending() {
        // given
        User user = createDefaultUser();

        // when
        boolean result = user.isFirstProfileCompletion();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("ACTIVE 상태일 때 프로필 최초 작성 여부가 false를 반환한다")
    void isFirstProfileCompletion_active() {
        // given
        User user = createDefaultUser();
        user.completeProfile();

        // when
        boolean result = user.isFirstProfileCompletion();

        // then
        assertThat(result).isFalse();
    }

    private User createDefaultUser() {
        return User.create(
                OAuthProvider.KAKAO,
                "kakao_12345",
                "test@example.com",
                "홍길동",
                "테스터"
        );
    }

    private UserProfile createDefaultProfile(User user) {
        return UserProfile.create(
                user,
                "https://example.com/image.jpg",
                "안녕하세요",
                "경험 설명",
                5,
                true
        );
    }
}