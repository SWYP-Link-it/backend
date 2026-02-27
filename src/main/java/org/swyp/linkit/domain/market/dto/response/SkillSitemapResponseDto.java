package org.swyp.linkit.domain.market.dto.response;

import java.time.LocalDateTime;

import org.swyp.linkit.domain.user.entity.UserSkill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "스킬 사이트맵 응답")
public class SkillSitemapResponseDto {

    @Schema(description = "스킬 ID", example = "1")
    private Long skillId;

    @Schema(description = "최종 수정일시")
    private LocalDateTime modifiedAt;

    public static SkillSitemapResponseDto from(UserSkill userSkill) {
        return SkillSitemapResponseDto.builder()
                .skillId(userSkill.getId())
                .modifiedAt(userSkill.getModifiedAt())
                .build();
    }
}
