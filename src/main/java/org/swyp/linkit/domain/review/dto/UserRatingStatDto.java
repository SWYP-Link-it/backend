package org.swyp.linkit.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.swyp.linkit.domain.review.entity.UserRatingStat;

@Getter
@AllArgsConstructor
public class UserRatingStatDto {

    private Long userProfileId;
    // UserRatingStat Entity가 존재하지 않을 시 null
    private Long userRatingStatId;
    private double avgRating;

    public static UserRatingStatDto from(UserRatingStat entity) {
        double rawAvg = entity.calculateAvgRating();

        // 소수점 한 자리 까지 표현
        double truncatedAvg = (int) (rawAvg * 10) / 10.0;

        return new UserRatingStatDto(entity.getUserProfileId(), entity.getId(), truncatedAvg);
    }

    public static UserRatingStatDto empty(Long userProfileId){
        return new UserRatingStatDto(userProfileId, null, 0.0);
    }
}
