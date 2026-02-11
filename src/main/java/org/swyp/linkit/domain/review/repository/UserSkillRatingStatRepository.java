package org.swyp.linkit.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.linkit.domain.review.entity.UserSkillRatingStat;

public interface UserSkillRatingStatRepository extends JpaRepository<UserSkillRatingStat, Long> {
}
