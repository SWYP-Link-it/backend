package org.swyp.linkit.domain.market.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.user.entity.UserSkill;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "스킬 요약 정보 응답 DTO")
public class SkillSummaryDto {

    @Schema(description = "스킬 ID", example = "2")
    private Long skillId;

    @Schema(description = "스킬명", example = "React")
    private String skillName;

    public static SkillSummaryDto from(UserSkill userSkill) {
        return SkillSummaryDto.builder()
                .skillId(userSkill.getId())
                .skillName(userSkill.getSkillName())
                .build();
    }
}