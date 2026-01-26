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

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(nullable = false, unique = true, length = 100)
    private String nickname;

    @Column(name = "user_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AvailableSchedule> availableSchedules = new ArrayList<>();

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

    // 닉네임 변경
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // 프로필 이미지 변경
    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // 회원가입 완료 처리
    public void activateAccount() {
        if (this.userStatus == UserStatus.PROFILE_PENDING) {
            this.userStatus = UserStatus.ACTIVE;
        }
    }

    // 가능 일정 추가
    public void addAvailableSchedule(AvailableSchedule schedule) {
        if (schedule == null) return;

        if (!this.availableSchedules.contains(schedule)) {
            this.availableSchedules.add(schedule);
        }

        if (schedule.getUser() != this) {
            schedule.assignUser(this);
        }
    }

    // 가능 일정 제거
    public void removeAvailableSchedule(AvailableSchedule schedule) {
        if (schedule == null) return;

        this.availableSchedules.remove(schedule);
        if (schedule.getUser() == this) {
            schedule.assignUser(null);
        }
    }

    // 모든 가능 일정 제거
    public void clearAvailableSchedules() {
        for (AvailableSchedule schedule : new ArrayList<>(this.availableSchedules)) {
            removeAvailableSchedule(schedule);
        }
    }

    // 회원 탈퇴 처리
    public void withdraw() {
        this.clearAvailableSchedules();
        this.userStatus = UserStatus.WITHDRAWN;
        this.deletedAt = LocalDateTime.now();
    }

    // 프로필 최초 작성 여부 확인
    public boolean isFirstProfileCompletion() {
        return this.userStatus == UserStatus.PROFILE_PENDING;
    }
}