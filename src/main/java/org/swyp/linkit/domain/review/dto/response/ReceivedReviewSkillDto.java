package org.swyp.linkit.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "받은 리뷰의 스킬 정보")
public class ReceivedReviewSkillDto {

    @Schema(description = "스킬 ID", example = "1")
    private Long skillId;

    @Schema(description = "스킬 이름", example = "React")
    private String skillName;

    @Schema(description = "평균 평점", example = "4.7")
    private Double avgRating;

    @Schema(description = "리뷰 개수", example = "3")
    private Integer ratingCount;

    public static ReceivedReviewSkillDto of(Long skillId, String skillName, Double avgRating, Integer ratingCount) {
        return ReceivedReviewSkillDto.builder()
                .skillId(skillId)
                .skillName(skillName)
                .avgRating(avgRating)
                .ratingCount(ratingCount)
                .build();
    }
}
