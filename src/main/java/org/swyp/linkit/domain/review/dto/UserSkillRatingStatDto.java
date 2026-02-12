package org.swyp.linkit.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.swyp.linkit.domain.review.entity.UserSkillRatingStat;

@Getter
@AllArgsConstructor
public class UserSkillRatingStatDto {

    private Long userSkillId;
    // UserSkillRatingStat Entity가 존재하지 않을 시 null
    private Long userSkillRatingStatId;
    private double avgRating;

    // 각 평점 퍼센티지 (정수형)
    private int star1Percentage;
    private int star2Percentage;
    private int star3Percentage;
    private int star4Percentage;
    private int star5Percentage;

    public static UserSkillRatingStatDto from(UserSkillRatingStat entity) {
        double rawAvg = entity.calculateAvgRating();

        // 소수점 한 자리 까지 표현
        double truncatedAvg = Math.round(rawAvg * 10) / 10.0;

        return new UserSkillRatingStatDto(
                entity.getUserSkillId(),
                entity.getId(),
                truncatedAvg,
                (int) Math.round(entity.calculateStarPercentage(entity.getStar1Count())),
                (int) Math.round(entity.calculateStarPercentage(entity.getStar2Count())),
                (int) Math.round(entity.calculateStarPercentage(entity.getStar3Count())),
                (int) Math.round(entity.calculateStarPercentage(entity.getStar4Count())),
                (int) Math.round(entity.calculateStarPercentage(entity.getStar5Count()))
        );
    }

    public static UserSkillRatingStatDto empty(Long userSkillId) {
        return new UserSkillRatingStatDto(
                userSkillId,
                null,
                0.0,
                0,
                0,
                0,
                0,
                0
        );
    }
}
