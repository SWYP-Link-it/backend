package org.swyp.linkit.domain.user.dto.response;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.swyp.linkit.domain.review.dto.response.UserSkillRatingResponseDto;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.domain.user.entity.SkillProficiency;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.entity.UserSkillImage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "사용자 스킬 응답")
public class UserSkillResponseDto {

    @Schema(description = "스킬 ID", example = "1")
    private Long id;

    @Schema(description = "스킬 카테고리 타입", example = "DEVELOPMENT")
    private SkillCategoryType skillCategoryType;

    @Schema(description = "스킬 카테고리 이름", example = "개발")
    private String skillCategoryName;

    @Schema(description = "스킬명", example = "React")
    private String skillName;

    @Schema(description = "스킬 제목", example = "React 고급 강의")
    private String skillTitle;

    @Schema(description = "스킬 숙련도", example = "HIGH")
    private SkillProficiency skillProficiency;

    @Schema(description = "스킬 소개", example = "5년 경력의 React 개발자입니다.")
    private String skillDescription;

    @Schema(description = "스킬 거래 시간 (분)", example = "60")
    private Integer exchangeDuration;

    @Schema(description = "장터 노출 여부", example = "true")
    private Boolean isVisible;

    @Schema(description = "스킬 이미지 URL 목록", example = "[\"https://...\", \"https://...\"]")
    private List<String> imageUrls;

    @Schema(description = "스킬별 평점 정보")
    private UserSkillRatingResponseDto skillRating;

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
                .isVisible(userSkill.getIsVisible())
                .imageUrls(userSkill.getImages().stream()
                        .sorted(Comparator.comparing(UserSkillImage::getImageOrder))
                        .map(UserSkillImage::getImageUrl)
                        .collect(Collectors.toList()))
                .skillRating(UserSkillRatingResponseDto.from(userSkill.getUserSkillRatingStat()))
                .build();
    }
}