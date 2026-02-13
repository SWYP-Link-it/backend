package org.swyp.linkit.domain.review.service;

import org.swyp.linkit.domain.review.dto.ReviewDto;
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
     * 받은 리뷰 페이징 조회
     * skillId = Null 이면 모든 스킬에 대한 리뷰 조회
     * todo skillId required = false
     */
    ReviewDetailsResponseDto getReceivedReviews(Long userId, Long skillId, Long cursorId, int size);

    /**
     * 작성한 리뷰 페이징 조회
     */
    ReviewDetailsResponseDto getWrittenReviews(Long userId, Long cursorId, int size);

    /**
     * 스킬 장터 스킬별 리뷰 페이징
     * todo skillId Required = true
     */
    ReviewDetailsResponseDto getSkillReviews(Long skillId, Long cursorId, int size);

}
