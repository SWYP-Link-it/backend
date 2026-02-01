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
@Schema(description = "스킬 카드 응답")
public class SkillCardResponseDto {

    @Schema(description = "스킬 ID", example = "1")
    private Long skillId;

    @Schema(description = "프로필 사진 URL", example = "https://...")
    private String profileImageUrl;

    @Schema(description = "닉네임", example = "개발자123")
    private String nickname;

    @Schema(description = "스킬 제목", example = "React 고급 강의")
    private String skillTitle;

    @Schema(description = "스킬명", example = "React")
    private String skillName;

    public static SkillCardResponseDto from(UserSkill userSkill) {
        return SkillCardResponseDto.builder()
                .skillId(userSkill.getId())
                .profileImageUrl(userSkill.getUserProfile().getUser().getProfileImageUrl())
                .nickname(userSkill.getUserProfile().getUser().getNickname())
                .skillTitle(userSkill.getSkillTitle())
                .skillName(userSkill.getSkillName())
                .build();
    }
}