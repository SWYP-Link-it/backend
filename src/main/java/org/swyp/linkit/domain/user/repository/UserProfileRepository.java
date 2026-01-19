package org.swyp.linkit.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.user.entity.UserProfile;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // 사용자 ID로 프로필 조회
    Optional<UserProfile> findByUserId(Long userId);

    // 사용자 ID로 프로필 존재 여부 확인
    boolean existsByUserId(Long userId);

    // 사용자 ID로 프로필 삭제
    void deleteByUserId(Long userId);

    // 사용자 ID로 프로필과 사용자 함께 조회
    @Query("SELECT up FROM UserProfile up JOIN FETCH up.user WHERE up.user.id = :userId")
    Optional<UserProfile> findByUserIdWithUser(@Param("userId") Long userId);
}
