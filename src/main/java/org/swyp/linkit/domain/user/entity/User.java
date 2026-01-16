package org.swyp.linkit.domain.user.entity;

import org.swyp.linkit.global.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"oauth_provider", "oauth_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(nullable = false, unique = true, length = 100)
    private String nickname;

    @Column(name = "user_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToOne(mappedBy = "user")
    private UserProfile userProfile;

    @Builder(access = AccessLevel.PRIVATE)
    private User(OAuthProvider oauthProvider, String oauthId, String email,
                 String name, String nickname, UserStatus userStatus) {
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.userStatus = userStatus;
    }

    // OAuth 소셜 로그인으로 신규 사용자 생성
    public static User create(OAuthProvider oauthProvider, String oauthId,
                              String email, String name, String nickname) {
        return User.builder()
                .oauthProvider(oauthProvider)
                .oauthId(oauthId)
                .email(email)
                .name(name)
                .nickname(nickname)
                .userStatus(UserStatus.PROFILE_PENDING)
                .build();
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