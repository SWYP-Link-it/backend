package org.swyp.linkit.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;

@Entity
@Table(name = "skill_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkillCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_category_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_code", nullable = false, unique = true, length = 100)
    private SkillCategoryType categoryCode;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Builder(access = AccessLevel.PRIVATE)
    private SkillCategory(SkillCategoryType categoryCode, String categoryName) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
    }

    public static SkillCategory create(SkillCategoryType categoryCode) {
        return SkillCategory.builder()
                .categoryCode(categoryCode)
                .categoryName(categoryCode.getDisplayName())
                .build();
    }
}
