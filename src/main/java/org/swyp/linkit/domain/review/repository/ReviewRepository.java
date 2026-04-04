package org.swyp.linkit.domain.review.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.review.entity.Review;
import org.swyp.linkit.domain.review.repository.projection.ReviewDetailQuery;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     *  Review 존재 조회
     */
    boolean existsBySkillExchangeIdAndReviewerId(Long skillExchangeId, Long reviewerId);

    /**
     * Review 단건 조회(reviewerId, reviewId)
     */
    Optional<Review> findByReviewerIdAndId(Long reviewerId, Long id);

    /**
     * 작성한 Review 페이징 조회
     * cursor 기반
     */
    @Query("SELECT new org.swyp.linkit.domain.review.repository.projection.ReviewDetailQuery" +
            "(r.id, u.nickname, s.skillName, r.content, r.rating, r.createdAt) " +
            "FROM Review r " +
            "LEFT JOIN User u ON u.id = r.revieweeId " + // 멘토의 정보 조회
            "LEFT JOIN UserSkill s ON r.revieweeSkillId = s.id " +
            "WHERE r.reviewerId = :reviewerId " +
            "AND (:cursorId IS NULL OR r.id < :cursorId) " +
            "ORDER BY r.id DESC")
    Slice<ReviewDetailQuery> findAllByReviewerId(@Param("reviewerId") Long reviewerId,
                                                 @Param("cursorId") Long cursorId,
                                                 Pageable pageable);

    /**
     *  스킬 별 받은 Review 페이징 조회
     *  skillId = NULL 이면 모든 Review 조회
     *  cursor 기반
     */
    @Query("SELECT new org.swyp.linkit.domain.review.repository.projection.ReviewDetailQuery" +
            "(r.id, u.nickname, s.skillName, r.content, r.rating, r.createdAt) " +
            "FROM Review r " +
            "LEFT JOIN User u ON u.id = r.reviewerId " + // 멘티의 정보 조회
            "LEFT JOIN UserSkill s ON r.revieweeSkillId = s.id " +
            "WHERE r.revieweeId = :revieweeId " +
            "AND (:skillId IS NULL OR r.revieweeSkillId = :skillId) " +
            "AND (:cursorId IS NULL OR r.id < :cursorId) " +
            "ORDER BY r.id DESC")
    Slice<ReviewDetailQuery> findAllByRevieweeId(@Param("revieweeId") Long revieweeId,
                                                 @Param("skillId") Long skillId,
                                                 @Param("cursorId") Long cursorId,
                                                 Pageable pageable);

    /**
     *  스킬 별 Review 페이징 조회
     *  cursor 기반
     */
    @Query("SELECT new org.swyp.linkit.domain.review.repository.projection.ReviewDetailQuery" +
            "(r.id, u.nickname, s.skillName, r.content, r.rating, r.createdAt) " +
            "FROM Review r " +
            "LEFT JOIN User u ON u.id = r.reviewerId " + // 작성자 정보 조회
            "LEFT JOIN UserSkill s ON r.revieweeSkillId = s.id " +
            "WHERE r.revieweeSkillId = :skillId " +
            "AND (:cursorId IS NULL OR r.id < :cursorId) " +
            "ORDER BY r.id DESC")
    Slice<ReviewDetailQuery> findAllBySkillId(@Param("skillId") Long skillId,
                                              @Param("cursorId") Long cursorId,
                                              Pageable pageable);
}