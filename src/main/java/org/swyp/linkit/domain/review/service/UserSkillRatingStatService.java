package org.swyp.linkit.domain.review.service;

import org.swyp.linkit.domain.review.dto.UserSkillRatingStatDto;

import java.util.Map;
import java.util.Set;

public interface UserSkillRatingStatService {

    /**
     * userSkillRatingStat Update
     */
    void updateUserSkillRating(Long userSkillId, int rating);

    /**
     *  유저 스킬 별 평점 조회
     *
     *  평점 정보 존재 시 소수점 한 자리까지 표현됩니다.
     *  평점 정보 존재 하지 않을 시 0.0 으로 처리됩니다.
     *  1 ~ 5 점의 비율은 정수형으로 처리됩니다.
     */
    UserSkillRatingStatDto getUserSkillRating(Long userSkillId);

    /**
     * userSkillRatingStat Decrease
     */
    void decreaseUserSkillRating(Long userSkillId, int rating);

    /**
     * userSkillRatingStat change
     */
    void changeUserSkillRating(Long userSkillId, int oldRating, int newRating);

    /**
     * 여러 스킬의 평점을 한 번에 조회
     */
    Map<Long, UserSkillRatingStatDto> getUserSkillRatings(Set<Long> userSkillIds);
}
