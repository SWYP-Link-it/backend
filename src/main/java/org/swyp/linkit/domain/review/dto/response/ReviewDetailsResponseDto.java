package org.swyp.linkit.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;
import org.swyp.linkit.domain.review.dto.UserSkillRatingStatDto;
import org.swyp.linkit.domain.review.service.projection.ReviewDetailQuery;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "스킬 거래 리뷰 페이징 응답")
public class ReviewDetailsResponseDto {

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "다음 페이지를 호출할 때 사용할 커서 값", example = "5")
    private Long nextCursor;

    @Schema(description = "리뷰 상세 목록")
    private List<ReviewDetailResponseDto> contents;

    public static ReviewDetailsResponseDto from(Slice<ReviewDetailQuery> slice,
                                                Map<Long, UserSkillRatingStatDto> skillRatingMap){
        List<ReviewDetailResponseDto> contents = slice.stream()
                .map(query -> ReviewDetailResponseDto.from(
                        query,
                        skillRatingMap.getOrDefault(query.skillId(), UserSkillRatingStatDto.empty(query.skillId()))
                ))
                .toList();
        return new ReviewDetailsResponseDto(
                slice.hasNext(),
                slice.hasNext() ? getNextCursor(slice) : null,
                contents);
    }

    private static Long getNextCursor(Slice<ReviewDetailQuery> slice) {
        return slice.isEmpty() ? null : slice.getContent().get(slice.getNumberOfElements() - 1).reviewId();
    }
}
