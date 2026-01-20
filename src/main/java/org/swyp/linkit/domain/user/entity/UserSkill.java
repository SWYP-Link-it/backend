package org.swyp.linkit.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;

@Entity
@Table(
        name = "user_skill",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "skill_category_id", "skill_name"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSkill extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_skill_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="skill_category_id", nullable = false)
    private SkillCategory skillCategory;

    @Column(name="skill_name", nullable = false, length = 100)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(name="skill_level", nullable = false, length = 20)
    private SkillLevel skillLevel;

    @Column(name="skill_description", columnDefinition = "TEXT")
    private String skillDescription;

    @Column(name="is_visible", nullable = false)
    private Boolean isVisible;

    @Builder(access = AccessLevel.PRIVATE)
    private UserSkill(User user, SkillCategory skillCategory, String skillName,
                      SkillLevel skillLevel, String skillDescription, Boolean isVisible) {
        this.user = user;
        this.skillCategory = skillCategory;
        this.skillName = skillName;
        this.skillLevel = skillLevel;
        this.skillDescription = skillDescription;
        this.isVisible = isVisible;
    }

    public static UserSkill create(User user, SkillCategory skillCategory, String skillName,
                                   SkillLevel skillLevel, String skillDescription, Boolean isVisible) {
        return UserSkill.builder()
                .user(user)
                .skillCategory(skillCategory)
                .skillName(skillName)
                .skillLevel(skillLevel)
                .skillDescription(skillDescription)
                .isVisible(isVisible)
                .build();
    }

    public void update(String skillName, SkillLevel skillLevel, String skillDescription, Boolean isVisible) {
        this.skillName = skillName;
        this.skillLevel = skillLevel;
        this.skillDescription = skillDescription;
        this.isVisible = isVisible;
    }

    public void changeCategory(SkillCategory skillCategory) {
        this.skillCategory = skillCategory;
    }
}
