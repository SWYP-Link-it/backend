package org.swyp.linkit.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.swyp.linkit.domain.review.entity.UserRatingStat;

@Getter
@AllArgsConstructor
public class UserRatingStatDto {

    private Long userId;
    // UserRatingStat Entity가 존재하지 않을 시 null
    private Long userRatingStatId;
    private double avgRating;

    public static UserRatingStatDto from(UserRatingStat entity) {
        double rawAvg = entity.calculateAvgRating();

        // 소수점 한 자리 까지 표현
        double truncatedAvg = Math.round(rawAvg * 10) / 10.0;

        return new UserRatingStatDto(entity.getUserId(), entity.getId(), truncatedAvg);
    }

    public static UserRatingStatDto empty(Long userId){
        return new UserRatingStatDto(userId, null, 0.0);
    }
}
