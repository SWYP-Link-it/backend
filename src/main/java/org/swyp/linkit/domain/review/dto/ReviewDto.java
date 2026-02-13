package org.swyp.linkit.domain.review.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.review.dto.request.ReviewRequestDto;
import org.swyp.linkit.domain.review.dto.request.ReviewUpdateRequestDto;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class ReviewDto {

    private Long id;

    private Long skillExchangeId;

    private Long revieweeSkillId;

    private Long reviewerId;

    private Long revieweeId;

    private String content;

    private Integer rating;

    @Builder(access = AccessLevel.PRIVATE)
    private ReviewDto(Long skillExchangeId, Long revieweeSkillId, Long reviewerId,
                      Long revieweeId, String content, Integer rating) {
        this.skillExchangeId = skillExchangeId;
        this.revieweeSkillId = revieweeSkillId;
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.content = content;
        this.rating = rating;
    }

    public static ReviewDto ofCreate(Long reviewerId, ReviewRequestDto dto){
        return ReviewDto.builder()
                .skillExchangeId(dto.getSkillExchangeId())
                .revieweeSkillId(dto.getSkillId())
                .reviewerId(reviewerId)
                .revieweeId(dto.getMentorId())
                .content(dto.getContent())
                .rating(dto.getRating())
                .build();
    }

    public static ReviewDto ofUpdate(Long reviewerId, Long reviewId, ReviewUpdateRequestDto dto){
        return ReviewDto.builder()
                .id(reviewId)
                .reviewerId(reviewerId)
                .content(dto.getContent())
                .rating(dto.getRating())
                .build();
    }
}
