package org.swyp.linkit.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.user.entity.OAuthProvider;
import org.swyp.linkit.domain.user.entity.UserWithdrawalHistory;

public interface UserWithdrawalHistoryRepository extends JpaRepository<UserWithdrawalHistory, Long> {

    // OAuth 제공자와 OAuth ID로 탈퇴 이력 존재 여부 확인
    boolean existsByOauthProviderAndOauthId(
            @Param("oauthProvider") OAuthProvider oauthProvider,
            @Param("oauthId") String oauthId
    );
}