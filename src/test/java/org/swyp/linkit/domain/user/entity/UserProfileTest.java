//package org.swyp.linkit.domain.user.entity;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DisplayName("UserProfile 엔티티 테스트")
//class UserProfileTest {
//
//    @Test
//    @DisplayName("사용자 프로필을 생성한다")
//    void createUserProfile() {
//        // given
//        User user = createDefaultUser();
//        String profileImageUrl = "https://example.com/image.jpg";
//        String introduction = "안녕하세요";
//        String experienceDescription = "경험 설명";
//        Integer timesTaught = 5;
//        Boolean profileIsPublic = true;
//
//        // when
//        UserProfile profile = UserProfile.create(
//                user,
//                profileImageUrl,
//                introduction,
//                experienceDescription,
//                timesTaught,
//                profileIsPublic
//        );
//
//        // then
//        assertThat(profile.getUser()).isEqualTo(user);
//        assertThat(profile.getProfileImageUrl()).isEqualTo(profileImageUrl);
//        assertThat(profile.getIntroduction()).isEqualTo(introduction);
//        assertThat(profile.getExperienceDescription()).isEqualTo(experienceDescription);
//        assertThat(profile.getTimesTaught()).isEqualTo(timesTaught);
//        assertThat(profile.getProfileIsPublic()).isEqualTo(profileIsPublic);
//    }
//
//    @Test
//    @DisplayName("프로필 정보를 수정한다")
//    void updateProfile() {
//        // given
//        User user = createDefaultUser();
//        UserProfile profile = createDefaultProfile(user);
//
//        // when
//        String newImageUrl = "https://example.com/new_image.jpg";
//        String newIntroduction = "수정된 소개";
//        String newExperience = "수정된 경험";
//        Integer newTimesTaught = 10;
//        Boolean newIsPublic = false;
//
//        profile.updateProfile(
//                newImageUrl,
//                newIntroduction,
//                newExperience,
//                newTimesTaught,
//                newIsPublic
//        );
//
//        // then
//        assertThat(profile.getProfileImageUrl()).isEqualTo(newImageUrl);
//        assertThat(profile.getIntroduction()).isEqualTo(newIntroduction);
//        assertThat(profile.getExperienceDescription()).isEqualTo(newExperience);
//        assertThat(profile.getTimesTaught()).isEqualTo(newTimesTaught);
//        assertThat(profile.getProfileIsPublic()).isEqualTo(newIsPublic);
//    }
//
//    @Test
//    @DisplayName("프로필 정보를 null로 수정할 수 있다")
//    void updateProfile_withNull() {
//        // given
//        User user = createDefaultUser();
//        UserProfile profile = createDefaultProfile(user);
//
//        // when
//        profile.updateProfile(null, null, null, null, false);
//
//        // then
//        assertThat(profile.getProfileImageUrl()).isNull();
//        assertThat(profile.getIntroduction()).isNull();
//        assertThat(profile.getExperienceDescription()).isNull();
//        assertThat(profile.getTimesTaught()).isNull();
//        assertThat(profile.getProfileIsPublic()).isFalse();
//    }
//
//    private User createDefaultUser() {
//        return User.create(
//                OAuthProvider.KAKAO,
//                "kakao_12345",
//                "test@example.com",
//                "홍길동",
//                "테스터"
//        );
//    }
//
//    private UserProfile createDefaultProfile(User user) {
//        return UserProfile.create(
//                user,
//                "https://example.com/image.jpg",
//                "안녕하세요",
//                "경험 설명",
//                5,
//                true
//        );
//    }
//}