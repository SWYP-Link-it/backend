package org.swyp.linkit.domain.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.swyp.linkit.domain.review.dto.UserSkillRatingStatDto;
import org.swyp.linkit.domain.review.entity.UserSkillRatingStat;
import org.swyp.linkit.domain.review.repository.UserSkillRatingStatRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSkillRatingStatServiceImpl implements UserSkillRatingStatService {

    private final UserSkillRatingStatRepository userSkillRatingStatRepository;

    /**
     * 유저 스킬 별 평점 조회
     */
    @Override
    public UserSkillRatingStatDto getUserSkillRating(Long userSkillId) {
        Optional<UserSkillRatingStat> userSkillRatingStat = userSkillRatingStatRepository.findByUserSkillId(userSkillId);

        // DTO 변환에서 NULL 여부 처리
        return userSkillRatingStat
                .map(UserSkillRatingStatDto::from)
                .orElseGet(() -> UserSkillRatingStatDto.empty(userSkillId));
    }
}
