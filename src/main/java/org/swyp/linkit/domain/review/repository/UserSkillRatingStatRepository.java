package org.swyp.linkit.domain.review.repository;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.swyp.linkit.domain.review.entity.UserSkillRatingStat;

import java.util.Optional;

public interface UserSkillRatingStatRepository extends JpaRepository<UserSkillRatingStat, Long> {

    Optional<UserSkillRatingStat> findByUserSkillId(Long userSkillId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s " +
            "FROM UserSkillRatingStat s " +
            "WHERE s.userSkillId = :userSkillId")
    Optional<UserSkillRatingStat> findByUserSkillIdWithLock(@Param("userSkillId") Long userSkillId);
}
