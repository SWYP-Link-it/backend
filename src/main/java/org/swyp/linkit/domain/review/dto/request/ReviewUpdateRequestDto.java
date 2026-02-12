package org.swyp.linkit.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Schema(description = "스킬 거래 리뷰 수정 요청")
public class ReviewUpdateRequestDto {

    @Schema(description = "리뷰 내용 (선택 사항)", example = "수정된 리뷰 내용입니다.")
    private String content;

    @Schema(description = "평점 (1~5점)", example = "4")
    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 최대 5점 이하여야 합니다.")
    private Integer rating;
}
