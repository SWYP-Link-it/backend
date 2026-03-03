package org.swyp.linkit.domain.user.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.user.entity.OAuthProvider;
import org.swyp.linkit.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // OAuth 제공자와 OAuth ID로 사용자 조회
    Optional<User> findByOauthProviderAndOauthId(
            @Param("oauthProvider") OAuthProvider oauthProvider,
            @Param("oauthId") String oauthId
    );

    // 닉네임 존재 여부 확인 (중복 체크)
    boolean existsByNickname(String nickname);

    // 닉네임 존재 여부 확인 (본인 제외 중복 체크)
    boolean existsByNicknameAndIdNot(String nickname, Long id);

    // 닉네임으로 사용자 조회
    Optional<User> findByNickname(String nickname);

    // 닉네임 prefix로 사용자 목록 조회 (부하 테스트용)
    List<User> findByNicknameStartingWithOrderByIdAsc(String prefix, Pageable pageable);
}