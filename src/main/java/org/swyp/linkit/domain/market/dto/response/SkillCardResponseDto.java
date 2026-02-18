package org.swyp.linkit.domain.market.dto.response;

import org.swyp.linkit.domain.review.entity.UserSkillRatingStat;
import org.swyp.linkit.domain.user.entity.UserSkill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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

		@Schema(description = "평균 평점", example = "3.6")
		private double avgRating;

    public static SkillCardResponseDto from(UserSkill userSkill) {
				UserSkillRatingStat ratingStat = userSkill.getUserSkillRatingStat();
				double avgRating = (ratingStat != null)
				? Math.round(ratingStat.calculateAvgRating() * 10) / 10.0
				: 0.0;
        
				return SkillCardResponseDto.builder()
                .skillId(userSkill.getId())
                .profileImageUrl(userSkill.getOwnerProfileImageUrl())
                .nickname(userSkill.getOwnerNickname())
                .skillTitle(userSkill.getSkillTitle())
                .skillName(userSkill.getSkillName())
								.avgRating(avgRating)
                .build();
    }
}