package org.swyp.linkit.domain.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.swyp.linkit.domain.review.dto.UserSkillRatingStatDto;
import org.swyp.linkit.domain.review.entity.UserSkillRatingStat;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSkillRatingStatServiceImpl implements UserSkillRatingStatService {

    /**
     * 유저 스킬 별 평점 조회
     */
    @Override
    public UserSkillRatingStatDto getUserSkillRating(UserSkillRatingStat userSkillRatingStat) {
        // DTO 변환에서 NULL 여부 처리
        return UserSkillRatingStatDto.from(userSkillRatingStat);
    }
}
