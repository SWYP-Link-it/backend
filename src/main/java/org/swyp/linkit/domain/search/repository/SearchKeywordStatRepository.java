package org.swyp.linkit.domain.search.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.search.entity.SearchKeywordStat;
import org.swyp.linkit.domain.search.repository.projection.PopularKeywordView;

import java.time.LocalDate;
import java.util.List;

public interface SearchKeywordStatRepository extends JpaRepository<SearchKeywordStat, Long> {

    // 검색어 카운트 증가 (UPSERT)
    @Modifying
    @Query(value = """
        INSERT INTO search_keyword_stat (stat_date, keyword, search_count)
        VALUES (:statDate, :keyword, 1)
        ON DUPLICATE KEY UPDATE search_count = search_count + 1
    """, nativeQuery = true)
    void upsertIncrement(@Param("statDate") LocalDate statDate,
                         @Param("keyword") String keyword);

    // 최근 N일 기준 인기 검색어 조회
    @Query("""
        SELECT s.keyword AS keyword, SUM(s.searchCount) AS totalCount
        FROM SearchKeywordStat s
        WHERE s.statDate >= :startDate
        GROUP BY s.keyword
        ORDER BY SUM(s.searchCount) DESC
    """)
    List<PopularKeywordView> findPopularKeywords(
            @Param("startDate") LocalDate startDate,
            Pageable pageable);
}