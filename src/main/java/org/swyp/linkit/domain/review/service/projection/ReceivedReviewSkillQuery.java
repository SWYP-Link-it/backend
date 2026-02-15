package org.swyp.linkit.domain.review.service.projection;

public record ReceivedReviewSkillQuery(
    Long skillId,
    String skillName,
    Long userSkillRatingStatId
) {
}