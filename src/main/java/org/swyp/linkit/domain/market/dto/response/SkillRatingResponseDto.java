package org.swyp.linkit.domain.market.dto.response;

import org.swyp.linkit.domain.review.dto.UserSkillRatingStatDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "스킬별 평점 응답")
public class SkillRatingResponseDto {

    @Schema(description = "평균 평점", example = "4.5")
    private double avgRating;

    @Schema(description = "1점 비율 (%)", example = "5")
    private int star1Percentage;

    @Schema(description = "2점 비율 (%)", example = "10")
    private int star2Percentage;

    @Schema(description = "3점 비율 (%)", example = "15")
    private int star3Percentage;

    @Schema(description = "4점 비율 (%)", example = "30")
    private int star4Percentage;

    @Schema(description = "5점 비율 (%)", example = "40")
    private int star5Percentage;

    public static SkillRatingResponseDto from(UserSkillRatingStatDto dto) {
        return SkillRatingResponseDto.builder()
                .avgRating(dto.getAvgRating())
                .star1Percentage(dto.getStar1Percentage())
                .star2Percentage(dto.getStar2Percentage())
                .star3Percentage(dto.getStar3Percentage())
                .star4Percentage(dto.getStar4Percentage())
                .star5Percentage(dto.getStar5Percentage())
                .build();
    }

    public static SkillRatingResponseDto of(double avgRating, int star1, int star2, int star3, int star4, int star5) {
        return SkillRatingResponseDto.builder()
                .avgRating(avgRating)
                .star1Percentage(star1)
                .star2Percentage(star2)
                .star3Percentage(star3)
                .star4Percentage(star4)
                .star5Percentage(star5)
                .build();
    }
}
