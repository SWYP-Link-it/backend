package org.swyp.linkit.domain.search.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.search.entity.SkillViewStat;
import org.swyp.linkit.domain.search.repository.projection.PopularSkillView;

public interface SkillViewStatRepository extends JpaRepository<SkillViewStat, Long> {

    // 스킬 조회수 누적 반영 (UPSERT, Redis flush용)
    @Modifying
    @Query(value = """
        INSERT INTO skill_view_stat (stat_date, skill_id, view_count)
        VALUES (:statDate, :skillId, :count)
        ON DUPLICATE KEY UPDATE view_count = view_count + :count
    """, nativeQuery = true)
    void upsertAdd(@Param("statDate") LocalDate statDate,
                   @Param("skillId") Long skillId,
                   @Param("count") long count);

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
