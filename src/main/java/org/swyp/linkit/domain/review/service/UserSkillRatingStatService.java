package org.swyp.linkit.domain.review.service;

import org.swyp.linkit.domain.review.dto.UserSkillRatingStatDto;
import org.swyp.linkit.domain.review.entity.UserSkillRatingStat;

public interface UserSkillRatingStatService {


    /**
     *  유저 스킬 별 평점 조회
     *
     *  [재영님 필독]
     *  호출 시 UserSkillRatingStat을 JOIN FETCH로 넘겨주세요.
     *  Entity가 Null 일 경우 감안하여 로직 처리하고 UserSkillRating 반환하겠습니다.
     */
    UserSkillRatingStatDto getUserSkillRating(UserSkillRatingStat userSkillRatingStat);
}
