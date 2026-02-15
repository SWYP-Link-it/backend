package org.swyp.linkit.domain.review.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "받은 리뷰의 스킬 목록 응답")
public class ReceivedReviewSkillsResponseDto {

    @Schema(description = "스킬 목록")
    private List<ReceivedReviewSkillDto> skills;

    public static ReceivedReviewSkillsResponseDto from(List<ReceivedReviewSkillDto> skills) {
        return new ReceivedReviewSkillsResponseDto(skills);
    }
}
