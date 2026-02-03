package org.swyp.linkit.domain.search.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.search.entity.SkillViewStat;
import org.swyp.linkit.domain.search.repository.projection.PopularSkillView;

import java.time.LocalDate;
import java.util.List;

public interface SkillViewStatRepository extends JpaRepository<SkillViewStat, Long> {

    // 스킬 조회수 증가 (UPSERT)
    @Modifying
    @Query(value = """
        INSERT INTO skill_view_stat (stat_date, skill_id, view_count)
        VALUES (:statDate, :skillId, 1)
        ON DUPLICATE KEY UPDATE view_count = view_count + 1
    """, nativeQuery = true)
    void upsertIncrement(@Param("statDate") LocalDate statDate,
                         @Param("skillId") Long skillId);

    // 최근 N일 기준 인기 스킬 조회
    @Query("""
        SELECT s.skillId AS skillId, SUM(s.viewCount) AS totalViewCount
        FROM SkillViewStat s
        WHERE s.statDate >= :startDate
        GROUP BY s.skillId
        ORDER BY SUM(s.viewCount) DESC
    """)
    List<PopularSkillView> findPopularSkills(
            @Param("startDate") LocalDate startDate,
            Pageable pageable);
}