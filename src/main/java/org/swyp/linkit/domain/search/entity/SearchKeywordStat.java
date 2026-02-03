package org.swyp.linkit.domain.search.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "search_keyword_stat",
        uniqueConstraints = @UniqueConstraint(columnNames = {"stat_date", "keyword"})
)
public class SearchKeywordStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "keyword", nullable = false, length = 50)
    private String keyword;

    @Column(name = "search_count", nullable = false)
    private Long searchCount;

    @Builder(access = AccessLevel.PRIVATE)
    private SearchKeywordStat(LocalDate statDate, String keyword, Long searchCount) {
        this.statDate = statDate;
        this.keyword = keyword;
        this.searchCount = searchCount;
    }

    public static SearchKeywordStat create(LocalDate statDate, String keyword) {
        return SearchKeywordStat.builder()
                .statDate(statDate)
                .keyword(keyword)
                .searchCount(1L)
                .build();
    }

    public void increment() {
        this.searchCount++;
    }
}