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
public class UserSkillRatingStat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_skill_rating_stat_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userSkillId;

    @Column(nullable = false)
    private Integer ratingSum;

    @Column(nullable = false)
    private Integer ratingCount;

    @Column(nullable = false)
    private Integer star1Count;

    @Column(nullable = false)
    private Integer star2Count;

    @Column(nullable = false)
    private Integer star3Count;

    @Column(nullable = false)
    private Integer star4Count;

    @Column(nullable = false)
    private Integer star5Count;

    @Builder(access = AccessLevel.PRIVATE)
    private UserSkillRatingStat(Long userSkillId, int ratingSum, int ratingCount,
                                int s1, int s2, int s3, int s4, int s5) {
        this.userSkillId = userSkillId;
        this.ratingSum = ratingSum;
        this.ratingCount = ratingCount;
        this.star1Count = s1;
        this.star2Count = s2;
        this.star3Count = s3;
        this.star4Count = s4;
        this.star5Count = s5;
    }

    // == 생성 메서드 ==
    public static UserSkillRatingStat create(Long userSkillId, int initialRating) {
        UserSkillRatingStat stat = UserSkillRatingStat.builder()
                .userSkillId(userSkillId)
                .ratingSum(initialRating)
                .ratingCount(1)
                .s1(0).s2(0).s3(0).s4(0).s5(0)
                .build();

        // 각 평점 별 초기 Update 처리
        stat.incrementStarCount(initialRating);
        return stat;
    }

    /**
     *  비즈니스 메서드
     */
    // ratingSum, ratingCount Update
    public void updateRating(int additionalRating) {
        this.ratingSum += additionalRating;
        this.ratingCount++;
        incrementStarCount(additionalRating);
    }

    // 각 평점 별 Update (requestDto 에서 검증 하기에 따로 예외 던지지 않음)
    private void incrementStarCount(int rating) {
        switch (rating) {
            case 1 -> this.star1Count++;
            case 2 -> this.star2Count++;
            case 3 -> this.star3Count++;
            case 4 -> this.star4Count++;
            case 5 -> this.star5Count++;
        }
    }

    // 평점 평균 계산
    public double calculateAvgRating(){
        // 생성 시점에 ratingSum, ratingCount가 설정되기에 0으로 나누기 방어 로직 생략
        return (double) this.ratingSum / this.ratingCount;
    }

    // 특정 평점 비율 계산
    public double calculateStarPercentage(int starCount) {
        // 생성 시점에 ratingSum, ratingCount가 설정되기에 0으로 나누기 방어 로직 생략
        return (double) starCount / this.ratingCount * 100;
    }
}
