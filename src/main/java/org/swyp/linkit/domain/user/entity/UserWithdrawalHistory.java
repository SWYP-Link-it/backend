package org.swyp.linkit.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;

import java.time.LocalDateTime;
@Entity
@Table(
        name = "user_withdrawal_history",
        indexes = {
                @Index(name = "idx_oauth_provider_id", columnList = "oauth_provider, oauth_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserWithdrawalHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_withdrawal_history_id")
    private Long id;

    @Column(name = "oauth_provider", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private OAuthProvider oauthProvider;

    @Column(name = "oauth_id", nullable = false)
    private String oauthId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(name = "withdrawn_at", nullable = false)
    private LocalDateTime withdrawnAt;

    @Builder(access = AccessLevel.PRIVATE)
    private UserWithdrawalHistory(
            OAuthProvider oauthProvider,
            String oauthId,
            Long userId,
            String email,
            LocalDateTime withdrawnAt
    ) {
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
        this.userId = userId;
        this.email = email;
        this.withdrawnAt = withdrawnAt;
    }

    // 탈퇴 이력 생성
    public static UserWithdrawalHistory create(User user) {
        return UserWithdrawalHistory.builder()
                .oauthProvider(user.getOauthProvider())
                .oauthId(user.getOauthId())
                .userId(user.getId())
                .email(user.getEmail())
                .withdrawnAt(LocalDateTime.now())
                .build();
    }
}