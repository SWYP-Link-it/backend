package org.swyp.linkit.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.user.entity.OAuthProvider;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserStatus;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    // OAuth 제공자와 OAuth ID로 활성 사용자 조회 (탈퇴 회원 제외)
    Optional<User> findByOauthProviderAndOauthIdAndUserStatusNot(
            @Param("oauthProvider") OAuthProvider oauthProvider,
            @Param("oauthId") String oauthId,
            @Param("userStatus") UserStatus userStatus
    );

    // 닉네임 존재 여부 확인 (중복 체크)
    boolean existsByNickname(String nickname);

}