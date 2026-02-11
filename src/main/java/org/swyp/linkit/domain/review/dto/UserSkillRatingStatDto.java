package org.swyp.linkit.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.swyp.linkit.domain.review.entity.UserSkillRatingStat;

@Getter
@AllArgsConstructor
public class UserSkillRatingStatDto {

    // UserSkillRatingStat Entity가 존재하지 않을 시 null
    private Long userSkillRatingId;
    private int avgRating;
    private int star1Count;
    private int star2Count;
    private int star3Count;
    private int star4Count;
    private int star5Count;

    public static UserSkillRatingStatDto from(UserSkillRatingStat entity) {
        if (entity == null) {
            return new UserSkillRatingStatDto(null, 0, 0, 0, 0, 0, 0);
        }

        return new UserSkillRatingStatDto(
                entity.getId(),
                entity.calculateAvgRating(),
                entity.getStar1Count(),
                entity.getStar2Count(),
                entity.getStar3Count(),
                entity.getStar4Count(),
                entity.getStar5Count()
        );
    }
}
