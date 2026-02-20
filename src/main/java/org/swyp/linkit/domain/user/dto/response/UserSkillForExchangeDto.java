package org.swyp.linkit.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.domain.user.entity.UserSkill;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "교환용 스킬 정보 응답 DTO")
public class UserSkillForExchangeDto {

    @Schema(description = "스킬 ID", example = "1")
    private Long skillId;

    @Schema(description = "스킬명", example = "React")
    private String skillName;

    @Schema(description = "스킬 거래 시간 (분)", example = "60")
    private Integer exchangeDuration;

    @Schema(description = "크레딧 가격 (30분당 1크레딧)", example = "2")
    private Integer creditPrice;

    @Schema(description = "스킬 카테고리 타입", example = "DEVELOPMENT")
    private SkillCategoryType skillCategoryType;

    public static UserSkillForExchangeDto from(UserSkill userSkill) {
        return UserSkillForExchangeDto.builder()
                .skillId(userSkill.getId())
                .skillName(userSkill.getSkillName())
                .exchangeDuration(userSkill.getExchangeDuration())
                .creditPrice(calculateCreditPrice(userSkill.getExchangeDuration()))
                .skillCategoryType(userSkill.getSkillCategory().getCategoryType())
                .build();
    }

    // 크레딧 가격 계산 (30분당 1크레딧)
    private static int calculateCreditPrice(Integer exchangeDuration) {
        if (exchangeDuration == null || exchangeDuration <= 0) {
            return 0;
        }
        return (int) Math.ceil(exchangeDuration / 30.0);
    }
}