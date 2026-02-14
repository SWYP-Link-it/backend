package org.swyp.linkit.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.review.dto.UserSkillRatingStatDto;
import org.swyp.linkit.domain.review.service.projection.ReviewDetailQuery;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "스킬 거래 리뷰 상세 내역")
public class ReviewDetailResponseDto {

    @Schema(description = "리뷰 식별자(ID)", example = "1")
    Long reviewId;

    @Schema(description = "리뷰 작성자 닉네임", example = "닉네임123")
    String reviewerNickname;

    @Schema(description = "거래한 스킬 이름", example = "React")
    String skillName;

    @Schema(description = "거래한 스킬 ID", example = "1")
    Long skillId;

    @Schema(description = "거래한 스킬의 평점 정보")
    UserSkillRatingStatDto skillRating;

    @Schema(description = "리뷰 내용", example = "친절하게 잘 알려주셨습니다.")
    String content;

    @Schema(description = "리뷰 평점", example = "5")
    Integer rating;

    @Schema(description = "리뷰 생성 날짜 및 시간", example = "2026-01-25T12:00:00")
    LocalDateTime createdDateTime;

    public static ReviewDetailResponseDto from(ReviewDetailQuery result, UserSkillRatingStatDto skillRating){
        return ReviewDetailResponseDto.builder()
                .reviewId(result.reviewId())
                .reviewerNickname(result.reviewerNickname())
                .skillName(result.skillName())
                .skillId(result.skillId())
                .skillRating(skillRating)
                .content(result.content())
                .rating(result.rating())
                .createdDateTime(result.createdAt().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }
}
