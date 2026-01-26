package org.swyp.linkit.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.linkit.domain.user.entity.SkillCategory;

@Repository
public interface SkillCategoryRepository extends JpaRepository<SkillCategory, Long> {
}