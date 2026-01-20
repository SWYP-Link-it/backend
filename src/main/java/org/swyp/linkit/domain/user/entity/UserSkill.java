package org.swyp.linkit.domain.user.entity;

import jakarta.persistence.*;
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

    // 사용자 스킬 생성
    public static UserSkill create(SkillCategory skillCategory, String skillName,
                                   SkillLevel skillLevel, String skillDescription, Boolean isVisible) {
        return UserSkill.builder()
                .skillCategory(skillCategory)
                .skillName(skillName)
                .skillLevel(skillLevel)
                .skillDescription(skillDescription)
                .isVisible(isVisible)
                .build();
    }

    // 사용자 스킬의 정보 수정
    public void update(String skillName, SkillLevel skillLevel, String skillDescription, Boolean isVisible) {
        this.skillName = skillName.trim();
        this.skillLevel = skillLevel;
        this.skillDescription = skillDescription;
        this.isVisible = isVisible;
    }

    // 사용자 스킬의 카테고리를 변경
    public void changeCategory(SkillCategory skillCategory) {
        this.skillCategory = skillCategory;
    }

    // 사용자 연관관계 설정
    protected void assignUser(User user) {
        this.user = user;
    }
}
