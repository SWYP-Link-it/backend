package org.swyp.linkit.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.domain.user.entity.UserSkill;

import jakarta.persistence.LockModeType;

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
						"LEFT JOIN FETCH us.userSkillRatingStat " +
            "WHERE us.isVisible = true " +
            "ORDER BY us.createdAt DESC")
    List<UserSkill> findAllVisibleSkills();

    // 카테고리별 노출 중인 스킬 목록 조회 (최신순)
    @Query("SELECT us FROM UserSkill us " +
            "JOIN FETCH us.userProfile up " +
            "JOIN FETCH up.user u " +
            "JOIN FETCH us.skillCategory sc " +
						"LEFT JOIN FETCH us.userSkillRatingStat " +
            "WHERE us.isVisible = true " +
            "AND sc.categoryType = :categoryType " +
            "ORDER BY us.createdAt DESC")
    List<UserSkill> findVisibleSkillsByCategory(@Param("categoryType") SkillCategoryType categoryType);

		// 키워드로 노출 중인 스킬 검색 (부분 일치, 대소문자 무시, 스킬명 또는 제목)
    @Query("SELECT us FROM UserSkill us " +
            "JOIN FETCH us.userProfile up " +
            "JOIN FETCH up.user u " +
            "JOIN FETCH us.skillCategory " +
						"LEFT JOIN FETCH us.userSkillRatingStat " +
            "WHERE us.isVisible = true " +
            "AND (LOWER(us.skillName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(us.skillTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY us.createdAt DESC")
    List<UserSkill> searchVisibleSkillsByName(@Param("keyword") String keyword);

		// 카테고리 + 키워드로 노출 중인 스킬 검색 (부분 일치, 대소문자 무시, 스킬명 또는 제목)
    @Query("SELECT us FROM UserSkill us " +
            "JOIN FETCH us.userProfile up " +
            "JOIN FETCH up.user u " +
            "JOIN FETCH us.skillCategory sc " +
						"LEFT JOIN FETCH us.userSkillRatingStat " +
            "WHERE us.isVisible = true " +
            "AND sc.categoryType = :categoryType " +
            "AND (LOWER(us.skillName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(us.skillTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY us.createdAt DESC")
    List<UserSkill> findVisibleSkillsByCategoryAndKeyword(
            @Param("categoryType") SkillCategoryType categoryType,
            @Param("keyword") String keyword
    );

		/**
		 * 스킬 장터 목록 커서 기반 페이징 조회
		 * - isVisible = true 인 스킬만 조회
		 * - categoryType : null 이면 전체 카테고리 조회, 값이 있으면 해당 카테고리만 조회
		 * - keyword : null 이면 전체 조회, 값이 있으면 skillName 또는 skillTitle 부분 일치 검색
		 * - cursorId : null 이면 최신 데이터부터 조회 (첫 페이지), 값이 있으면 해당 id 미만 조회
		 * - UserProfile, User, SkillCategory Fetch Join
		 */
		@Query("SELECT us FROM UserSkill us " +
						"JOIN FETCH us.userProfile up " +
						"JOIN FETCH up.user u " +
						"JOIN FETCH us.skillCategory sc " +
						"LEFT JOIN FETCH us.userSkillRatingStat " +
						"WHERE us.isVisible = true " +
						"AND (:categoryType IS NULL OR sc.categoryType = :categoryType) " +
						"AND (:keyword IS NULL OR LOWER(us.skillName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
						"     OR LOWER(us.skillTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
						"AND (:cursorId IS NULL OR us.id < :cursorId) " +
						"ORDER BY us.id DESC")
		Slice<UserSkill> findVisibleSkillsWithCursor(
						@Param("categoryType") SkillCategoryType categoryType,
						@Param("keyword") String keyword,
						@Param("cursorId") Long cursorId,
						Pageable pageable);

    // 특정 사용자의 노출 중인 스킬 목록 조회 (교환 요청용)
    @Query("SELECT us FROM UserSkill us " +
            "JOIN FETCH us.userProfile up " +
            "JOIN FETCH us.skillCategory sc " +
            "WHERE up.user.id = :userId " +
            "AND us.isVisible = true " +
            "ORDER BY us.createdAt")
    List<UserSkill> findVisibleSkillsAndSkillCategoryByUserId(@Param("userId") Long userId);

    // 특정 사용자의 모든 스킬 목록 조회 (스킬 평점 포함)
    @Query("SELECT us FROM UserSkill us " +
            "JOIN FETCH us.userProfile up " +
            "LEFT JOIN FETCH us.userSkillRatingStat " +
            "WHERE up.user.id = :userId " +
            "ORDER BY us.createdAt ASC")
    List<UserSkill> findAllSkillsByUserId(@Param("userId") Long userId);

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
            "ORDER BY us.createdAt ASC")
    List<UserSkill> findVisibleSkillsWithImagesByUserId(@Param("userId") Long userId);

}