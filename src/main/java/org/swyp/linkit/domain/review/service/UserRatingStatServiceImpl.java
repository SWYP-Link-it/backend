package org.swyp.linkit.domain.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.swyp.linkit.domain.review.dto.UserRatingStatDto;
import org.swyp.linkit.domain.review.entity.UserRatingStat;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRatingStatServiceImpl implements UserRatingStatService{

    /**
     *  유저 전체 평점 조회
     */
    @Override
    public UserRatingStatDto getUserRating(UserRatingStat userRatingStat) {
        return UserRatingStatDto.from(userRatingStat);
    }
}
