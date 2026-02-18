package org.swyp.linkit.domain.review.service;

import org.swyp.linkit.domain.review.dto.ReviewDto;
import org.swyp.linkit.domain.review.dto.response.ReceivedReviewRatingInfoResponseDto;
import org.swyp.linkit.domain.review.dto.response.ReviewDetailsResponseDto;
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

    /**
     * 유저가 받은 리뷰 스킬별 페이징 조회
     * skillId = Null 이면 모든 스킬에 대한 리뷰 조회
     */
    ReviewDetailsResponseDto getReceivedReviews(Long userId, Long skillId, Long cursorId, int size);

    /**
     * 유저가 작성한 리뷰 페이징 조회
     */
    ReviewDetailsResponseDto getWrittenReviews(Long userId, Long cursorId, int size);

    /**
     * 스킬별 리뷰 페이징 조회
     */
    ReviewDetailsResponseDto getSkillReviews(Long skillId, Long cursorId, int size);

    /**
     * 받은 리뷰 페이지 - 유저 전체 평점 및 스킬별 평점 탭 목록 조회
     */
    ReceivedReviewRatingInfoResponseDto getReceivedReviewRatingInfo(Long userId);

}
