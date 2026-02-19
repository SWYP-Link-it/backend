package org.swyp.linkit.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.review.repository.projection.ReviewDetailQuery;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "스킬 거래 리뷰 상세 내역")
public class ReviewDetailResponseDto {

    @Schema(description = "리뷰 식별자(ID)", example = "1")
    Long reviewId;

    @Schema(description = "리뷰 작성자 닉네임", example = "1")
    String reviewerNickname;

    @Schema(description = "거래한 스킬 이름", example = "1")
    String skillName;

    @Schema(description = "리뷰 내용", example = "1")
    String content;

    @Schema(description = "리뷰 평점", example = "3")
    Integer rating;

    @Schema(description = "리뷰 생성 날짜 및 시간", example = "2026-01-25T12:00:00")
    LocalDateTime createdDateTime;

    public static ReviewDetailResponseDto from(ReviewDetailQuery result){
        return ReviewDetailResponseDto.builder()
                .reviewId(result.reviewId())
                .reviewerNickname(result.reviewerNickname())
                .skillName(result.skillName())
                .content(result.content())
                .rating(result.rating())
                .createdDateTime(result.createdAt().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }
}
