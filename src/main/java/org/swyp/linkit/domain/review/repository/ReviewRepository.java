package org.swyp.linkit.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.linkit.domain.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     *  Review 존재 조회
     */
    boolean existsBySkillExchangeIdAndReviewerId(Long skillExchangeId, Long reviewerId);
}
