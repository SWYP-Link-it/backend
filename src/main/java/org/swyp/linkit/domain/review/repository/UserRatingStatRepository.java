package org.swyp.linkit.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.linkit.domain.review.entity.UserRatingStat;

import java.util.Optional;

public interface UserRatingStatRepository extends JpaRepository<UserRatingStat, Long> {

    Optional<UserRatingStat> findByUserId(Long userId);
}
