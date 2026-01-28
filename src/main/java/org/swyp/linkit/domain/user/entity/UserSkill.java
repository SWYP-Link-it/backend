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
                @UniqueConstraint(columnNames = {"user_profile_id", "skill_category_id", "skill_name"})
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
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_category_id", nullable = false)
    private SkillCategory skillCategory;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(name = "skill_title", nullable = false, length = 40)
    private String skillTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_proficiency", nullable = false, length = 20)
    private SkillProficiency skillProficiency;

    @Column(name = "skill_description", length = 500)
    private String skillDescription;

    @Column(name = "exchange_duration", nullable = false)
    private Integer exchangeDuration;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible;

    @Builder(access = AccessLevel.PRIVATE)
    private UserSkill(UserProfile userProfile, SkillCategory skillCategory, String skillName,
                      String skillTitle, SkillProficiency skillProficiency, String skillDescription,
                      Integer exchangeDuration, Long viewCount, Boolean isVisible) {
        this.userProfile = userProfile;
        this.skillCategory = skillCategory;
        this.skillName = skillName;
        this.skillTitle = skillTitle;
        this.skillProficiency = skillProficiency;
        this.skillDescription = skillDescription;
        this.exchangeDuration = exchangeDuration;
        this.viewCount = viewCount;
        this.isVisible = isVisible;
    }

    // 사용자 스킬 생성
    public static UserSkill create(SkillCategory skillCategory, String skillName,
                                   String skillTitle, SkillProficiency skillProficiency,
                                   String skillDescription, Integer exchangeDuration) {
        return UserSkill.builder()
                .skillCategory(skillCategory)
                .skillName(skillName)
                .skillTitle(skillTitle)
                .skillProficiency(skillProficiency)
                .skillDescription(skillDescription)
                .exchangeDuration(exchangeDuration)
                .viewCount(0L)
                .isVisible(true)
                .build();
    }

    // 사용자 스킬 수정
    public void update(SkillCategory skillCategory, String skillName, String skillTitle,
                       SkillProficiency skillProficiency, String skillDescription,
                       Integer exchangeDuration) {
        this.skillCategory = skillCategory;
        this.skillName = skillName.trim();
        this.skillTitle = skillTitle.trim();
        this.skillProficiency = skillProficiency;
        this.skillDescription = skillDescription;
        this.exchangeDuration = exchangeDuration;
    }

    // 조회수 증가
    public void incrementViewCount() {
        this.viewCount++;
    }

    // 스킬 장터 노출 토글
    public void toggleVisibility() {
        this.isVisible = !this.isVisible;
    }

    // 사용자 프로필 연관관계 설정
    protected void assignUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
}