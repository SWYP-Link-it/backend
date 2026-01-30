package org.swyp.linkit.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.domain.user.entity.SkillProficiency;
import org.swyp.linkit.domain.user.entity.UserSkill;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "사용자 스킬 응답")
public class UserSkillResponseDto {

    @Schema(description = "스킬 ID", example = "1")
    private Long id;

    @Schema(description = "스킬 카테고리 타입", example = "MUSIC")
    private SkillCategoryType skillCategoryType;

    @Schema(description = "스킬 카테고리명", example = "음악")
    private String skillCategoryName;

    @Schema(description = "스킬명", example = "기타")
    private String skillName;

    @Schema(description = "스킬 제목", example = "초보자를 위한 기타 레슨")
    private String skillTitle;

    @Schema(description = "스킬 숙련도", example = "MEDIUM")
    private SkillProficiency skillProficiency;

    @Schema(description = "스킬 소개", example = "5년 경력의 기타리스트입니다.")
    private String skillDescription;

    @Schema(description = "스킬 거래 시간 (분)", example = "60")
    private Integer exchangeDuration;

    @Schema(description = "조회수", example = "0")
    private Long viewCount;

    @Schema(description = "스킬 장터 노출 여부", example = "true")
    private Boolean isVisible;

    public static UserSkillResponseDto from(UserSkill userSkill) {
        return UserSkillResponseDto.builder()
                .id(userSkill.getId())
                .skillCategoryType(userSkill.getSkillCategory().getCategoryType())
                .skillCategoryName(userSkill.getSkillCategory().getCategoryName())
                .skillName(userSkill.getSkillName())
                .skillTitle(userSkill.getSkillTitle())
                .skillProficiency(userSkill.getSkillProficiency())
                .skillDescription(userSkill.getSkillDescription())
                .exchangeDuration(userSkill.getExchangeDuration())
                .viewCount(userSkill.getViewCount())
                .isVisible(userSkill.getIsVisible())
                .build();
    }
}