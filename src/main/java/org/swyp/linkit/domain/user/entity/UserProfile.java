package org.swyp.linkit.domain.user.entity;

import org.swyp.linkit.global.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100)
    private String introduction;

    @Column(name = "experience_description", length = 100)
    private String experienceDescription;

    @Column(name = "times_taught")
    private Integer timesTaught;

    @Column(name = "profile_is_public", nullable = false)
    private Boolean profileIsPublic;

    @Builder(access = AccessLevel.PRIVATE)
    private UserProfile(User user, String introduction, String experienceDescription,
                        Integer timesTaught, Boolean profileIsPublic) {
        this.user = user;
        this.introduction = introduction;
        this.experienceDescription = experienceDescription;
        this.timesTaught = timesTaught;
        this.profileIsPublic = profileIsPublic;
    }

    // 사용자 프로필 생성
    public static UserProfile create(User user, String introduction, String experienceDescription,
                                     Integer timesTaught, Boolean profileIsPublic) {
        return UserProfile.builder()
                .user(user)
                .introduction(introduction)
                .experienceDescription(experienceDescription)
                .timesTaught(timesTaught)
                .profileIsPublic(profileIsPublic)
                .build();
    }

    // 사용자 프로필 수정
    public void updateProfile(String introduction, String experienceDescription,
                              Integer timesTaught, Boolean profileIsPublic) {
        this.introduction = introduction;
        this.experienceDescription = experienceDescription;
        this.timesTaught = timesTaught;
        this.profileIsPublic = profileIsPublic;
    }

    // 사용자 연관관계 설정
    protected void assignUser(User user) {
        this.user = user;
    }
}