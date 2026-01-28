package org.swyp.linkit.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.domain.user.entity.SkillProficiency;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "사용자 스킬 등록 요청")
public class UserSkillRequestDto {

    @Schema(description = "스킬 카테고리 타입 (DEVELOPMENT, DESIGN, EDITING, MARKETING, LANGUAGE, FINANCE, SPORTS, MUSIC, ETC)", example = "MUSIC")
    @NotNull(message = "스킬 카테고리는 필수입니다.")
    private SkillCategoryType skillCategoryType;

    @Schema(description = "스킬명 (최대 20자)", example = "기타")
    @NotBlank(message = "스킬명은 필수입니다.")
    @Size(max = 20, message = "스킬명은 20자 이하여야 합니다.")
    private String skillName;

    @Schema(description = "스킬 제목 (1~39자)", example = "초보자를 위한 기타 레슨")
    @NotBlank(message = "스킬 제목은 필수입니다.")
    @Size(min = 1, max = 39, message = "스킬 제목은 1자 이상 39자 이하여야 합니다.")
    private String skillTitle;

    @Schema(description = "스킬 숙련도 (HIGH, MEDIUM, LOW)", example = "MEDIUM")
    @NotNull(message = "스킬 숙련도는 필수입니다.")
    private SkillProficiency skillProficiency;

    @Schema(description = "스킬 소개 (최대 100자)", example = "5년 경력의 기타리스트입니다.")
    @Size(max = 100, message = "스킬 소개는 100자 이하여야 합니다.")
    private String skillDescription;

    @Schema(description = "스킬 거래 시간 (분 단위, 30/60/90/120/150/180)", example = "60")
    @NotNull(message = "스킬 거래 시간은 필수입니다.")
    private Integer exchangeDuration;
}