package org.swyp.linkit.domain.review.service;

import org.swyp.linkit.domain.review.dto.ReviewDto;
import org.swyp.linkit.domain.review.dto.response.ReviewResponseDto;

public interface ReviewService {

    /**
     *  리뷰, 평점 작성
     */
    ReviewResponseDto createReview(ReviewDto dto);

    /**
     *  리뷰, 평점 수정
     */
    ReviewResponseDto updateReview(ReviewDto dto);

    /**
     *  리뷰 삭제
     */
    void deleteReview(Long reviewId, Long reviewerId);
}
