package org.swyp.linkit.domain.user.entity;

import org.swyp.linkit.global.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"oauth_provider", "oauth_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "oauth_provider", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private OAuthProvider oauthProvider;

    @Column(name = "oauth_id", nullable = false)
    private String oauthId;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "profile_image_url", length = 100)
    private String profileImageUrl;

    @Column(nullable = false, unique = true, length = 100)
    private String nickname;

    @Column(name = "user_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile userProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSkill> userSkills = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private User(OAuthProvider oauthProvider, String oauthId, String email,
                 String name, String profileImageUrl, String nickname, UserStatus userStatus) {
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.nickname = nickname;
        this.userStatus = userStatus;
    }

    // OAuth 소셜 로그인으로 신규 사용자 생성
    public static User create(OAuthProvider oauthProvider, String oauthId, String email,
                              String name, String profileImageUrl, String nickname) {
        return User.builder()
                .oauthProvider(oauthProvider)
                .oauthId(oauthId)
                .email(email)
                .name(name)
                .profileImageUrl(profileImageUrl)
                .nickname(nickname)
                .userStatus(UserStatus.PROFILE_PENDING)
                .build();
    }

    // OAuth 정보 업데이트 (이메일, 이름 변경 시)
    public void updateOAuthInfo(String email, String name) {
        this.email = email;
        this.name = name;
    }

    // 프로필 작성 완료 처리
    public void completeProfile() {
        if (this.userStatus == UserStatus.PROFILE_PENDING) {
            this.userStatus = UserStatus.ACTIVE;
        }
    }

    // 프로필 삭제
    public void removeProfile() {
        if (this.userProfile != null) {
            this.userProfile.assignUser(null);
            this.userProfile = null;
        }
    }

    // 사용자 프로필 연관관계 설정
    public void assignProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        if (userProfile != null && userProfile.getUser() != this) {
            userProfile.assignUser(this);
        }
    }

    // 사용자에게 스킬을 추가
    public void addUserSkill(UserSkill userSkill) {
        if (userSkill == null) return;

        if (!this.userSkills.contains(userSkill)) {
            this.userSkills.add(userSkill);
        }

        if (userSkill.getUser() != this) {
            userSkill.assignUser(this);
        }
    }

    // 사용자로부터 스킬을 제거
    public void removeUserSkill(UserSkill userSkill) {
        if (userSkill == null) return;

        this.userSkills.remove(userSkill);
        if (userSkill.getUser() == this) {
            userSkill.assignUser(null);
        }
    }

    // 사용자가 보유한 모든 스킬을 제거한다
    public void clearUserSkills() {
        for (UserSkill us : new ArrayList<>(this.userSkills)) {
            removeUserSkill(us);
        }
    }

    // 회원 탈퇴 처리 (Soft Delete)
    public void withdraw() {
        if (this.userProfile != null) {
            this.removeProfile();
        }
        this.userStatus = UserStatus.WITHDRAWN;
        this.deletedAt = LocalDateTime.now();
    }

    // 프로필 최초 작성 여부 확인
    public boolean isFirstProfileCompletion() {
        return this.userStatus == UserStatus.PROFILE_PENDING;
    }
}