package org.swyp.linkit.domain.user.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.domain.user.entity.UserSkill;

import java.util.List;
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

    // 노출 중인 스킬 목록 조회 (최신순)
    @Query("SELECT us FROM UserSkill us " +
            "JOIN FETCH us.userProfile up " +
            "JOIN FETCH up.user u " +
            "JOIN FETCH us.skillCategory " +
            "WHERE us.isVisible = true " +
            "ORDER BY us.createdAt DESC")
    List<UserSkill> findAllVisibleSkills();

    // 카테고리별 노출 중인 스킬 목록 조회 (최신순)
    @Query("SELECT us FROM UserSkill us " +
            "JOIN FETCH us.userProfile up " +
            "JOIN FETCH up.user u " +
            "JOIN FETCH us.skillCategory sc " +
            "WHERE us.isVisible = true " +
            "AND sc.categoryType = :categoryType " +
            "ORDER BY us.createdAt DESC")
    List<UserSkill> findVisibleSkillsByCategory(@Param("categoryType") SkillCategoryType categoryType);

    // 특정 사용자의 노출 중인 스킬 목록 조회 (교환 요청용)
    @Query("SELECT us FROM UserSkill us " +
            "JOIN FETCH us.userProfile up " +
            "WHERE up.user.id = :userId " +
            "AND us.isVisible = true " +
            "ORDER BY us.createdAt")
    List<UserSkill> findVisibleSkillsByUserId(@Param("userId") Long userId);

    // 스킬 ID로 노출 중인 스킬 상세 조회 (프로필, 사용자, 이미지 포함)
    @Query("SELECT us FROM UserSkill us " +
            "JOIN FETCH us.userProfile up " +
            "JOIN FETCH up.user u " +
            "LEFT JOIN FETCH us.images " +
            "WHERE us.id = :skillId " +
            "AND us.isVisible = true")
    Optional<UserSkill> findVisibleSkillDetailById(@Param("skillId") Long skillId);

    // 특정 사용자의 노출 중인 모든 스킬 조회 (이미지 포함)
    // 스킬 상세 페이지에서 다른 스킬 목록 표시용
    @Query("SELECT us FROM UserSkill us " +
            "JOIN FETCH us.userProfile up " +
            "LEFT JOIN FETCH us.images " +
            "WHERE up.user.id = :userId " +
            "AND us.isVisible = true " +
            "ORDER BY us.createdAt DESC")
    List<UserSkill> findVisibleSkillsWithImagesByUserId(@Param("userId") Long userId);

    // 스킬명으로 노출 중인 스킬 검색 (완전 일치, 대소문자 무시)
    @Query("SELECT us FROM UserSkill us " +
            "JOIN FETCH us.userProfile up " +
            "JOIN FETCH up.user u " +
            "JOIN FETCH us.skillCategory " +
            "WHERE us.isVisible = true " +
            "AND LOWER(us.skillName) = LOWER(:skillName) " +
            "ORDER BY us.createdAt DESC")
    List<UserSkill> searchVisibleSkillsByName(@Param("skillName") String skillName);

}
