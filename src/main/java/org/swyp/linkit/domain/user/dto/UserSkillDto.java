package org.swyp.linkit.domain.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.user.dto.request.UserSkillRequestDto;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.domain.user.entity.SkillProficiency;

@Getter
public class UserSkillDto {

    private SkillCategoryType skillCategoryType;
    private String skillName;
    private String skillTitle;
    private SkillProficiency skillProficiency;
    private String skillDescription;
    private Integer exchangeDuration;

    @Builder(access = AccessLevel.PRIVATE)
    private UserSkillDto(SkillCategoryType skillCategoryType, String skillName, String skillTitle,
                         SkillProficiency skillProficiency, String skillDescription,
                         Integer exchangeDuration) {
        this.skillCategoryType = skillCategoryType;
        this.skillName = skillName;
        this.skillTitle = skillTitle;
        this.skillProficiency = skillProficiency;
        this.skillDescription = skillDescription;
        this.exchangeDuration = exchangeDuration;
    }

    public static UserSkillDto from(UserSkillRequestDto requestDto) {
        return UserSkillDto.builder()
                .skillCategoryType(requestDto.getSkillCategoryType())
                .skillName(requestDto.getSkillName())
                .skillTitle(requestDto.getSkillTitle())
                .skillProficiency(requestDto.getSkillProficiency())
                .skillDescription(requestDto.getSkillDescription())
                .exchangeDuration(requestDto.getExchangeDuration())
                .build();
    }
}