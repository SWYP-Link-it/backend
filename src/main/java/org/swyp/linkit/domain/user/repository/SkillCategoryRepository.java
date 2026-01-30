package org.swyp.linkit.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.linkit.domain.user.entity.SkillCategory;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;

import java.util.Optional;

@Repository
public interface SkillCategoryRepository extends JpaRepository<SkillCategory, Long> {

    // categoryType으로 SkillCategory 조회
    Optional<SkillCategory> findByCategoryType(SkillCategoryType categoryType);
}