package org.swyp.linkit.domain.user.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.user.entity.UserSkill;

import java.util.Optional;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    // 스킬 ID와 프로필 ID로 스킬 조회 (권한 체크용)
    Optional<UserSkill> findByIdAndUserProfileId(Long id, Long userProfileId);

    // 스킬 ID와 사용자 ID로 스킬 조회 (권한 체크용)
    @Query("SELECT us FROM UserSkill us " +
            "JOIN us.userProfile up " +
            "WHERE us.id = :skillId AND up.user.id = :userId")
    Optional<UserSkill> findByIdAndUserId(
            @Param("skillId") Long skillId,
            @Param("userId") Long userId
    );

    // UserSkill ID로 UserProfile, User 포함하여 조회
    @Query("SELECT us FROM UserSkill us " +
            "JOIN FETCH us.userProfile up " +
            "JOIN FETCH up.user u " +
            "WHERE us.id = :id")
    Optional<UserSkill> findByIdWithProfileAndUser(@Param("id") Long id);

    // UserSkill ID로 UserProfile, User 포함하여 조회
    // 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT us FROM UserSkill us " +
            "JOIN FETCH us.userProfile up " +
            "JOIN FETCH up.user u " +
            "WHERE us.id = :id")
    Optional<UserSkill> findByIdWithProfileAndUserAndLock(@Param("id") Long id);
}
