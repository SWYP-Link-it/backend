package org.swyp.linkit.domain.review.service;

import org.swyp.linkit.domain.review.dto.UserRatingStatDto;

public interface UserRatingStatService {

    /**
     *  유저 전체 평점 조회
     *
     *  평점 정보 존재 시 소수점 한 자리까지 표현됩니다.
     *  평점 정보 존재 하지 않을 시 0.0 으로 처리됩니다.
     */
    UserRatingStatDto getUserRating(Long userProfileId);
}
