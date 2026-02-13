package org.swyp.linkit.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.review.entity.Review;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "리뷰 작성 응답")
public class ReviewResponseDto {

    @Schema(description = "스킬 거래 리뷰 ID", example = "1")
    private Long reviewId;

    @Schema(description = "리뷰 내용 (선택 사항)", example = "수업이 정말 유익했습니다.")
    private String content;

    @Schema(description = "평점 (1~5점)", example = "5")
    private Integer rating;

    public static ReviewResponseDto from(Review review){
        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .build();
    }
}
