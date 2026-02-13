package org.swyp.linkit.domain.review.service.projection;

import java.time.LocalDateTime;

public record ReviewDetailQuery(
    Long reviewId,
    String reviewerNickname,
    String skillName,
    String content,
    Integer rating,
    LocalDateTime createdAt
){
}

