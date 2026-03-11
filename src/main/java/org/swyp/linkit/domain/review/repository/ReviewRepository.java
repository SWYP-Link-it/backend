package org.swyp.linkit.domain.review.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.review.entity.Review;
import org.swyp.linkit.domain.review.repository.projection.ReviewDetailQuery;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Review 존재 조회
     */
    boolean existsBySkillExchangeIdAndReviewerId(Long skillExchangeId, Long reviewerId);

    /**
     * Review 단건 조회 (reviewerId, reviewId)
     */
    Optional<Review> findByReviewerIdAndId(Long reviewerId, Long id);

    /**
     * 작성한 Review 페이징 조회 (cursor 기반)
     */
    @Query("SELECT new org.swyp.linkit.domain.review.repository.projection.ReviewDetailQuery" +
            "(r.id, u.nickname, s.skillName, r.content, r.rating, r.createdAt) " +
            "FROM Review r " +
            "LEFT JOIN User u ON u.id = r.revieweeId " +
            "LEFT JOIN UserSkill s ON r.revieweeSkillId = s.id " +
            "WHERE r.reviewerId = :reviewerId " +
            "AND (:cursorId IS NULL OR r.id < :cursorId) " +
            "ORDER BY r.id DESC")
    Slice<ReviewDetailQuery> findAllByReviewerId(@Param("reviewerId") Long reviewerId,
                                                 @Param("cursorId") Long cursorId,
                                                 Pageable pageable);

    /**
     * 스킬별 받은 Review 페이징 조회 (cursor 기반)
     * skillId = NULL 이면 모든 Review 조회
     */
    @Query("SELECT new org.swyp.linkit.domain.review.repository.projection.ReviewDetailQuery" +
            "(r.id, u.nickname, s.skillName, r.content, r.rating, r.createdAt) " +
            "FROM Review r " +
            "LEFT JOIN User u ON u.id = r.reviewerId " +
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
     * 스킬별 Review 페이징 조회 (cursor 기반)
     */
    @Query("SELECT new org.swyp.linkit.domain.review.repository.projection.ReviewDetailQuery" +
            "(r.id, u.nickname, s.skillName, r.content, r.rating, r.createdAt) " +
            "FROM Review r " +
            "LEFT JOIN User u ON u.id = r.reviewerId " +
            "LEFT JOIN UserSkill s ON r.revieweeSkillId = s.id " +
            "WHERE r.revieweeSkillId = :skillId " +
            "AND (:cursorId IS NULL OR r.id < :cursorId) " +
            "ORDER BY r.id DESC")
    Slice<ReviewDetailQuery> findAllBySkillId(@Param("skillId") Long skillId,
                                              @Param("cursorId") Long cursorId,
                                              Pageable pageable);


    /**
     * 유저가 받은 리뷰 평균 평점 (리뷰 없으면 0.0)
     */
    @Query("SELECT COALESCE(AVG(r.rating), 0.0)" +
            " FROM Review r " +
            "WHERE r.revieweeId = :userId")
    double findAvgRatingByRevieweeId(@Param("userId") Long userId);


    /**
     * 스킬이 받은 리뷰 평균 평점 (리뷰 없으면 0.0)
     */
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) " +
            "FROM Review r " +
            "WHERE r.revieweeSkillId = :skillId")
    double findAvgRatingBySkillId(@Param("skillId") Long skillId);

    /**
     * 스킬별 별점 분포 조회
     * [rating(Integer), count(Long)] 형태의 Object[] 리스트 반환
     * 리뷰가 없는 별점은 결과에 포함되지 않음 (서비스에서 기본값 0 처리)
     */
    @Query("SELECT r.rating, COUNT(r) " +
            "FROM Review r " +
            "WHERE r.revieweeSkillId = :skillId GROUP BY r.rating")
    List<Object[]> findStarDistributionBySkillId(@Param("skillId") Long skillId);
}