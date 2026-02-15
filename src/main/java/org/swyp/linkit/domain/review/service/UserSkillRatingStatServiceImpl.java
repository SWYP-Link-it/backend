package org.swyp.linkit.domain.review.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.review.dto.UserSkillRatingStatDto;
import org.swyp.linkit.domain.review.entity.UserSkillRatingStat;
import org.swyp.linkit.domain.review.repository.UserSkillRatingStatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSkillRatingStatServiceImpl implements UserSkillRatingStatService {

    private final UserSkillRatingStatRepository userSkillRatingStatRepository;

    /**
     * userSkillRatingStat Update
     * (초기에 동시 리뷰 작성으로 인한 테이블 중복 방지)
     */
    @Transactional
    @Override
    public void updateUserSkillRating(Long userSkillId, int rating) {
        try{
            // 조회 및 update 시도
            userSkillRatingStatRepository.findByUserSkillId(userSkillId)
                    .ifPresentOrElse(
                            // 존재 시 update처리
                            stat -> stat.updateRating(rating),
                            () -> {
                                // 존재하지 않는다면 생성, 저장
                                userSkillRatingStatRepository.save(UserSkillRatingStat.create(userSkillId, rating));
                                // unique 제약 조건 확인 위해 flush
                                userSkillRatingStatRepository.flush();
                            }
                    );

        } catch (DataIntegrityViolationException e){
            // 다른 트랜잭션이 먼저 테이블을 생성했을 경우
            userSkillRatingStatRepository.findByUserSkillId(userSkillId)
                    .ifPresent(stat -> stat.updateRating(rating));
        }
    }

    /**
     * 유저 스킬 별 평점 조회
     */
    @Transactional(readOnly = true)
    @Override
    public UserSkillRatingStatDto getUserSkillRating(Long userSkillId) {
        Optional<UserSkillRatingStat> userSkillRatingStat = userSkillRatingStatRepository.findByUserSkillId(userSkillId);

        // DTO 변환에서 NULL 여부 처리
        return userSkillRatingStat
                .map(UserSkillRatingStatDto::from)
                .orElseGet(() -> UserSkillRatingStatDto.empty(userSkillId));
    }

    /**
     * userSkillRatingStat Decrease
     */
    @Transactional
    @Override
    public void decreaseUserSkillRating(Long userSkillId, int rating) {
        // 1. userSkillRatingStat 조회 및 감소 처리
        userSkillRatingStatRepository.findByUserSkillId(userSkillId)
                .ifPresent(stat -> stat.decreaseRating(rating));
    }

    /**
     * userSkillRatingStat change
     */
    @Transactional
    @Override
    public void changeUserSkillRating(Long userSkillId, int oldRating, int newRating) {
        // 1. userSkillRatingStat 조회 및 평점 수정 처리
        userSkillRatingStatRepository.findByUserSkillId(userSkillId)
                .ifPresent(stat -> {
                    stat.decreaseRating(oldRating);
                    stat.updateRating(newRating);
                });
    }
}
