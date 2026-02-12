package org.swyp.linkit.domain.user.entity;

import org.swyp.linkit.domain.review.entity.UserRatingStat;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_rating_stat_id", nullable = true, unique = true)
    private UserRatingStat userRatingStat;

    @Column(name = "experience_description", length = 100)
    private String experienceDescription;

    @Column(name = "times_taught", nullable = false)
    private Integer timesTaught;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_type", length = 20)
    private ExchangeType exchangeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_region", length = 50)
    private PreferredRegion preferredRegion;

    @Column(name = "detailed_location", length = 100)
    private String detailedLocation;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSkill> userSkills = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private UserProfile(String experienceDescription,
                        Integer timesTaught, ExchangeType exchangeType,
                        PreferredRegion preferredRegion, String detailedLocation) {
        this.experienceDescription = experienceDescription;
        this.timesTaught = timesTaught;
        this.exchangeType = exchangeType;
        this.preferredRegion = preferredRegion;
        this.detailedLocation = detailedLocation;
    }

    // 사용자 프로필 생성
    public static UserProfile create(User user, String experienceDescription,
                                     ExchangeType exchangeType, PreferredRegion preferredRegion,
                                     String detailedLocation) {
        UserProfile userProfile = UserProfile.builder()
                .experienceDescription(experienceDescription)
                .timesTaught(0)
                .exchangeType(exchangeType)
                .preferredRegion(preferredRegion)
                .detailedLocation(detailedLocation)
                .build();

        user.assignUserProfile(userProfile);

        return userProfile;
    }

    // UserRatingStat 연관관계 설정
    public void assignUserRatingStat(UserRatingStat userRatingStat){
        this.userRatingStat = userRatingStat;
    }

    // User 할당
    protected void assignUser(User user) {
        this.user = user;
    }

    // 사용자 프로필 수정
    public void update(String experienceDescription, ExchangeType exchangeType,
                       PreferredRegion preferredRegion, String detailedLocation) {
        this.experienceDescription = experienceDescription;
        this.exchangeType = exchangeType;
        this.preferredRegion = preferredRegion;
        this.detailedLocation = detailedLocation;
    }

    // 가르친 횟수 증가
    public void incrementTimesTaught() {
        this.timesTaught++;
    }

    // 사용자 스킬 추가
    public void addUserSkill(UserSkill userSkill) {
        if (userSkill == null) return;

        if (!this.userSkills.contains(userSkill)) {
            this.userSkills.add(userSkill);
        }

        if (userSkill.getUserProfile() != this) {
            userSkill.assignUserProfile(this);
        }
    }

    // 사용자 스킬 제거
    public void removeUserSkill(UserSkill userSkill) {
        if (userSkill == null) return;

        this.userSkills.remove(userSkill);
        if (userSkill.getUserProfile() == this) {
            userSkill.assignUserProfile(null);
        }
    }

    // 모든 사용자 스킬 제거
    public void clearUserSkills() {
        for (UserSkill skill : new ArrayList<>(this.userSkills)) {
            removeUserSkill(skill);
        }
    }
}