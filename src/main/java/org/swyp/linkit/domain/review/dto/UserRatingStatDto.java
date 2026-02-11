package org.swyp.linkit.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.swyp.linkit.domain.review.entity.UserRatingStat;

@Getter
@AllArgsConstructor
public class UserRatingStatDto {

    // UserRatingStat Entity가 존재하지 않을 시 null
    private Long userRatingStatId;
    private int avgRating;

    public static UserRatingStatDto from(UserRatingStat entity) {
        if (entity == null) {
            return new UserRatingStatDto(null, 0);
        }

        return new UserRatingStatDto(entity.getId(), entity.calculateAvgRating());
    }
}
