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
    private Long userId;

    @Column(nullable = false)
    private Integer ratingSum;

    @Column(nullable = false)
    private Integer ratingCount;

    @Builder(access = AccessLevel.PRIVATE)
    private UserRatingStat(Long userId, Integer ratingSum, int ratingCount) {
        this.userId = userId;
        this.ratingSum = ratingSum;
        this.ratingCount = ratingCount;
    }

    // == 생성 메서드 ==
    public static UserRatingStat create(Long userId, int initialRating) {
        return UserRatingStat.builder()
                .userId(userId)
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
    public double calculateAvgRating(){
        // 생성 시점에 ratingSum, ratingCount가 설정되기에 0으로 나누기 방어 로직 생략
        return (double) this.ratingSum / this.ratingCount;
    }

    // 평점 감소
    public void decreaseRating(int rating){
        if (this.ratingCount > 0) {
            this.ratingSum -= rating;
            this.ratingCount--;
        }
    }
}
