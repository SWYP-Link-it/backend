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
        name = "skill_view_stat",
        uniqueConstraints = @UniqueConstraint(columnNames = {"stat_date", "skill_id"})
)
public class SkillViewStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "skill_id", nullable = false)
    private Long skillId;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Builder(access = AccessLevel.PRIVATE)
    private SkillViewStat(LocalDate statDate, Long skillId, Long viewCount) {
        this.statDate = statDate;
        this.skillId = skillId;
        this.viewCount = viewCount;
    }

    public static SkillViewStat create(LocalDate statDate, Long skillId) {
        return SkillViewStat.builder()
                .statDate(statDate)
                .skillId(skillId)
                .viewCount(1L)
                .build();
    }

    public void increment() {
        this.viewCount++;
    }
}