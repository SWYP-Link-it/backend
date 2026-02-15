package org.swyp.linkit.domain.review.dto.response;

import org.swyp.linkit.domain.review.entity.UserSkillRatingStat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "스킬 평점 정보 응답")
public class UserSkillRatingResponseDto {

    @Schema(description = "평균 평점", example = "4.7")
    private Double avgRating;

    @Schema(description = "1점 비율 (%)", example = "0")
    private Integer star1Percentage;

    @Schema(description = "2점 비율 (%)", example = "0")
    private Integer star2Percentage;

    @Schema(description = "3점 비율 (%)", example = "0")
    private Integer star3Percentage;

    @Schema(description = "4점 비율 (%)", example = "33")
    private Integer star4Percentage;

    @Schema(description = "5점 비율 (%)", example = "67")
    private Integer star5Percentage;

    public static UserSkillRatingResponseDto from(UserSkillRatingStat entity) {
        if (entity == null) {
            return null;
        }

        double rawAvg = entity.calculateAvgRating();
        double truncatedAvg = Math.round(rawAvg * 10) / 10.0;

        return UserSkillRatingResponseDto.builder()
                .avgRating(truncatedAvg)
                .star1Percentage((int) Math.round(entity.calculateStarPercentage(entity.getStar1Count())))
                .star2Percentage((int) Math.round(entity.calculateStarPercentage(entity.getStar2Count())))
                .star3Percentage((int) Math.round(entity.calculateStarPercentage(entity.getStar3Count())))
                .star4Percentage((int) Math.round(entity.calculateStarPercentage(entity.getStar4Count())))
                .star5Percentage((int) Math.round(entity.calculateStarPercentage(entity.getStar5Count())))
                .build();
    }
}