package org.swyp.linkit.domain.review.dto.response;

import org.swyp.linkit.domain.review.entity.UserRatingStat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "유저 평점 정보 응답")
public class UserRatingResponseDto {

    @Schema(description = "평균 평점", example = "4.6")
    private Double avgRating;

    public static UserRatingResponseDto from(UserRatingStat entity) {
        if (entity == null) {
            return null;
        }

        double rawAvg = entity.calculateAvgRating();
        double truncatedAvg = Math.round(rawAvg * 10) / 10.0;

        return UserRatingResponseDto.builder()
                .avgRating(truncatedAvg)
                .build();
    }
}
