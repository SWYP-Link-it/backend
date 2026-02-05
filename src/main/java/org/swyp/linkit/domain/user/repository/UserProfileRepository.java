package org.swyp.linkit.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.linkit.domain.user.entity.UserProfile;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // 사용자 ID로 프로필 조회
    Optional<UserProfile> findByUserId(Long userId);

    // 사용자 ID로 프로필 존재 여부 확인
    boolean existsByUserId(Long userId);
}
