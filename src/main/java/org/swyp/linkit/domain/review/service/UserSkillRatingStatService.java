package org.swyp.linkit.domain.review.service;

import org.swyp.linkit.domain.review.dto.UserSkillRatingStatDto;

public interface UserSkillRatingStatService {


    /**
     *  유저 스킬 별 평점 조회
     *
     *  [재영님 필독]
     *  평점 정보 존재 시 소수점 한 자리까지 표현됩니다.
     *  평점 정보 존재 하지 않을 시 0.0 으로 처리됩니다.
     *  1 ~ 5 점의 비율은 정수형으로 처리됩니다.
     */
    UserSkillRatingStatDto getUserSkillRating(Long userSkillId);
}
