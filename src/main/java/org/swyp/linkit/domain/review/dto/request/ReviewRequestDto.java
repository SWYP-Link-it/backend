package org.swyp.linkit.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Schema(description = "스킬 거래 리뷰 작성 요청")
public class ReviewRequestDto {

    @Schema(description = "스킬 거래 ID", example = "1")
    @NotNull(message = "스킬 거래 ID는 필수입니다.")
    private Long skillExchangeId;

    @Schema(description = "멘토의 리뷰 대상 스킬 거래 ID", example = "1")
    @NotNull(message = "멘토의 리뷰 대상 스킬 ID는 필수입니다.")
    private Long skillId;

    @Schema(description = "리뷰 대상 멘토의 사용자 ID", example = "1")
    @NotNull(message = "리뷰 대상 멘토의 ID는 필수입니다.")
    private Long mentorId;

    @Schema(description = "리뷰 내용 (선택 사항)", example = "수업이 정말 유익했습니다.")
    @Size(max = 500, message = "리뷰 내용은 최대 500자까지 입력 가능합니다.")
    private String content;

    @Schema(description = "평점 (1~5점)", example = "5")
    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 최대 5점 이하여야 합니다.")
    private Integer rating;
}