package org.swyp.linkit.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.linkit.domain.review.entity.UserSkillRatingStat;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserSkillRatingStatRepository extends JpaRepository<UserSkillRatingStat, Long> {

    Optional<UserSkillRatingStat> findByUserSkillId(Long userSkillId);

    // 여러 스킬 ID로 일괄 조회
    List<UserSkillRatingStat> findByUserSkillIdIn(Set<Long> userSkillIds);
}
