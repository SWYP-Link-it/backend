package org.swyp.linkit.domain.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.user.entity.UserSkill;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "인기 스킬 응답")
public class PopularSkillDto {

    @Schema(description = "스킬 ID", example = "1")
    private Long skillId;

    @Schema(description = "닉네임", example = "개발자123")
    private String nickname;

    @Schema(description = "스킬 제목", example = "React 고급 강의")
    private String skillTitle;

    public static PopularSkillDto from(UserSkill userSkill) {
        return PopularSkillDto.builder()
                .skillId(userSkill.getId())
                .nickname(userSkill.getOwnerNickname())
                .skillTitle(userSkill.getSkillTitle())
                .build();
    }
}