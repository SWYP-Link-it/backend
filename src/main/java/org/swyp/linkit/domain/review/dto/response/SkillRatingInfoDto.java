package org.swyp.linkit.domain.review.dto.response;

import org.swyp.linkit.domain.review.dto.UserSkillRatingStatDto;
import org.swyp.linkit.domain.user.entity.UserSkill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "스킬별 평점 탭 정보")
public class SkillRatingInfoDto {

    @Schema(description = "스킬 ID", example = "1")
    private Long skillId;

    @Schema(description = "스킬명", example = "React")
    private String skillName;

    @Schema(description = "스킬 평균 평점", example = "4.5")
    private double avgRating;

    public static SkillRatingInfoDto of(UserSkill userSkill, UserSkillRatingStatDto ratingStatDto) {
        return SkillRatingInfoDto.builder()
                .skillId(userSkill.getId())
                .skillName(userSkill.getSkillName())
                .avgRating(ratingStatDto.getAvgRating())
                .build();
    }
}
