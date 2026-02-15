package org.swyp.linkit.domain.review.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.review.entity.Review;
import org.swyp.linkit.domain.review.service.projection.ReceivedReviewSkillQuery;
import org.swyp.linkit.domain.review.service.projection.ReviewDetailQuery;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     *  Review 존재 조회
     */
    boolean existsBySkillExchangeIdAndReviewerId(Long skillExchangeId, Long reviewerId);

    /**
     * 작성한 Review 페이징 조회
     * cursor 기반
     */
    @Query("SELECT new org.swyp.linkit.domain.review.service.projection.ReviewDetailQuery" +
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
    @Query("SELECT new org.swyp.linkit.domain.review.service.projection.ReviewDetailQuery" +
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
    @Query("SELECT new org.swyp.linkit.domain.review.service.projection.ReviewDetailQuery" +
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
        

    /**
     *  유저가 받은 리뷰의 스킬 목록 조회
     */
    @Query("SELECT new org.swyp.linkit.domain.review.service.projection.ReceivedReviewSkillQuery(" +
            "s.id, s.skillName) " +
            "FROM Review r " +
            "LEFT JOIN UserSkill s ON r.revieweeSkillId = s.id " +
            "WHERE r.revieweeId = :userId " +
            "GROUP BY s.id, s.skillName" +
            "ORDER BY s.id ASC")
    List<ReceivedReviewSkillQuery> findReceivedReviewSkillsByUserId(@Param("userId") Long userId);
}
