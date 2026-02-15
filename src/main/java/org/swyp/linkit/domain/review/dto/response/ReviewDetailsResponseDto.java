package org.swyp.linkit.domain.review.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;
import org.swyp.linkit.domain.review.service.projection.ReviewDetailQuery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "스킬 거래 리뷰 페이징 응답")
public class ReviewDetailsResponseDto {

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "다음 페이지를 호출할 때 사용할 커서 값", example = "5")
    private Long nextCursor;

    @Schema(description = "평균 평점 (받은 리뷰 조회 시: 전체 탭=사용자 전체 평점, 스킬 탭=해당 스킬 평점 / 작성한 리뷰 조회 시: null)", example = "4.6")
    private Double avgRating;

    @Schema(description = "리뷰 상세 목록")
    private List<ReviewDetailResponseDto> contents;

    // 작성한 리뷰용 (평점 없음)
    public static ReviewDetailsResponseDto from(Slice<ReviewDetailQuery> slice){
        List<ReviewDetailResponseDto> contents = slice.stream()
                .map(ReviewDetailResponseDto::from)
                .toList();
        return new ReviewDetailsResponseDto(
                slice.hasNext(),
                slice.hasNext() ? getNextCursor(slice) : null,
                null,
                contents);
    }

    // 받은 리뷰용 (평점 포함)
    public static ReviewDetailsResponseDto from(Slice<ReviewDetailQuery> slice, Double avgRating){
        List<ReviewDetailResponseDto> contents = slice.stream()
                .map(ReviewDetailResponseDto::from)
                .toList();
        return new ReviewDetailsResponseDto(
                slice.hasNext(),
                slice.hasNext() ? getNextCursor(slice) : null,
                avgRating,
                contents);
    }

    private static Long getNextCursor(Slice<ReviewDetailQuery> slice) {
        return slice.isEmpty() ? null : slice.getContent().get(slice.getNumberOfElements() - 1).reviewId();
    }
}
