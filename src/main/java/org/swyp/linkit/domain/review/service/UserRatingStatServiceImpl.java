package org.swyp.linkit.domain.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.review.dto.UserRatingStatDto;
import org.swyp.linkit.domain.review.entity.UserRatingStat;
import org.swyp.linkit.domain.review.repository.UserRatingStatRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRatingStatServiceImpl implements UserRatingStatService {

    private final UserRatingStatRepository userRatingStatRepository;

    /**
     * userRatingStat Update
     * (초기에 동시 리뷰 작성으로 인한 테이블 중복 방지)
     */
    @Transactional
    @Override
    public void updateUserRating(Long userId, int rating) {
        try{
            // 조회 및 update 시도
            userRatingStatRepository.findByUserId(userId)
                    .ifPresentOrElse(
                            // 존재 시 update처리
                            stat -> stat.updateRating(rating),
                            () -> {
                                // 존재하지 않는다면 생성, 저장
                                userRatingStatRepository.save(UserRatingStat.create(userId, rating));
                                // unique 제약 조건 확인 위해 flush
                                userRatingStatRepository.flush();
                            }
                    );

        } catch (DataIntegrityViolationException e){
            // 다른 트랜잭션이 먼저 테이블을 생성했을 경우
            userRatingStatRepository.findByUserId(userId)
                    .ifPresent(stat -> stat.updateRating(rating));
        }
    }

    /**
     *  유저 전체 평점 조회
     */
    @Transactional(readOnly = true)
    @Override
    public UserRatingStatDto getUserRating(Long userId) {
        // 1. userRatingStat 조회
        Optional<UserRatingStat> userRatingStat = userRatingStatRepository.findByUserId(userId);

        // 2. 존재 여부에 따라 DTO 매핑 처리
        return userRatingStat
                .map(UserRatingStatDto::from)
                .orElseGet(() -> UserRatingStatDto.empty(userId));
    }

    /**
     * userRatingStat decrease
     */
    @Transactional
    @Override
    public void decreaseUserRating(Long userId, int rating) {
        userRatingStatRepository.findByUserId(userId)
                .ifPresent(stat -> stat.decreaseRating(rating));
    }
}
