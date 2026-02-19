package org.swyp.linkit.domain.review.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "받은 리뷰 평점 및 스킬 탭 정보")
public class ReceivedReviewRatingInfoResponseDto {

    @Schema(description = "유저 전체 평균 평점", example = "4.2")
    private double userAvgRating;

    @Schema(description = "스킬별 평점 탭 목록")
    private List<SkillRatingInfoDto> skills;

    public static ReceivedReviewRatingInfoResponseDto of(double userAvgRating, List<SkillRatingInfoDto> skills) {
        return ReceivedReviewRatingInfoResponseDto.builder()
                .userAvgRating(userAvgRating)
                .skills(skills)
                .build();
    }
}
