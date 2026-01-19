package org.swyp.linkit.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.user.entity.OAuthProvider;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserStatus;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // OAuth 제공자와 OAuth ID로 사용자 조회
    Optional<User> findByOauthProviderAndOauthId(OAuthProvider oauthProvider, String oauthId);

    // OAuth 제공자와 OAuth ID로 활성 사용자 조회 (탈퇴 회원 제외)
    Optional<User> findByOauthProviderAndOauthIdAndUserStatusNot(
            OAuthProvider oauthProvider,
            String oauthId,
            UserStatus userStatus
    );

    // 닉네임으로 사용자 조회
    Optional<User> findByNickname(String nickname);

    // 닉네임 존재 여부 확인 (중복 체크)
    boolean existsByNickname(String nickname);

    // 사용자 ID로 프로필 포함 조회
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userProfile WHERE u.id = :id")
    Optional<User> findByIdWithProfile(@Param("id") Long id);

    // OAuth 제공자와 OAuth ID로 프로필 포함 조회
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userProfile " +
            "WHERE u.oauthProvider = :oauthProvider AND u.oauthId = :oauthId")
    Optional<User> findByOauthProviderAndOauthIdWithProfile(
            @Param("oauthProvider") OAuthProvider oauthProvider,
            @Param("oauthId") String oauthId
    );
}