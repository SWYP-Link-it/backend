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

    public static ReceivedReviewSkillDto of(Long skillId, String skillName) {
        return ReceivedReviewSkillDto.builder()
                .skillId(skillId)
                .skillName(skillName)
                .build();
    }
}
