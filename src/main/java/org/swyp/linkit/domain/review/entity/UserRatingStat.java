package org.swyp.linkit.domain.review.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserRatingStat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_rating_stat_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userProfileId;

    @Column(nullable = false)
    private Integer ratingSum;

    @Column(nullable = false)
    private Integer ratingCount;

    @Builder(access = AccessLevel.PRIVATE)
    private UserRatingStat(Long userProfileId, Integer ratingSum, int ratingCount) {
        this.userProfileId = userProfileId;
        this.ratingSum = ratingSum;
        this.ratingCount = ratingCount;
    }

    // == 생성 메서드 ==
    public static UserRatingStat create(Long userProfileId, int initialRating) {
        return UserRatingStat.builder()
                .userProfileId(userProfileId)
                .ratingSum(initialRating)
                .ratingCount(1)
                .build();
    }

    /**
     *  비즈니스 메서드
     */

    // ratingSum, ratingCount Update
    public void updateRating(int additionalRating){
        this.ratingSum += additionalRating;
        this.ratingCount++;
    }

    // 평점 평균 계산
    public int calculateAvgRating(){
        return this.ratingSum / this.ratingCount;
    }
}
