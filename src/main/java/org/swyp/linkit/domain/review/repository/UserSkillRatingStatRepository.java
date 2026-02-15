package org.swyp.linkit.domain.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.linkit.domain.review.entity.UserSkillRatingStat;

public interface UserSkillRatingStatRepository extends JpaRepository<UserSkillRatingStat, Long> {

    Optional<UserSkillRatingStat> findByUserSkillId(Long userSkillId);

    // 여러 스킬 ID로 일괄 조회
    List<UserSkillRatingStat> findByUserSkillIdIn(List<Long> userSkillIds);
}
