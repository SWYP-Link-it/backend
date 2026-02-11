package org.swyp.linkit.domain.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.swyp.linkit.domain.review.dto.UserRatingStatDto;
import org.swyp.linkit.domain.review.entity.UserRatingStat;
import org.swyp.linkit.domain.review.repository.UserRatingStatRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRatingStatServiceImpl implements UserRatingStatService{

    private final UserRatingStatRepository userRatingStatRepository;

    /**
     *  유저 전체 평점 조회
     */
    @Override
    public UserRatingStatDto getUserRating(Long userProfileId) {
        // 1. userRatingStat 조회
        Optional<UserRatingStat> userRatingStat = userRatingStatRepository.findByUserProfileId(userProfileId);

        // 2. 존재 여부에 따라 DTO 매핑 처리
        return userRatingStat
                .map(UserRatingStatDto::from)
                .orElseGet(() -> UserRatingStatDto.empty(userProfileId));
    }
}
