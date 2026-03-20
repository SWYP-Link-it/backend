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
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_exchange_review_id")
    private Long id;

    @Column(nullable = true, unique = false)
    private Long skillExchangeId;

    @Column(nullable = false)
    private Long revieweeSkillId;

    @Column(nullable = false)
    private Long reviewerId;

    @Column(nullable = false)
    private Long revieweeId;

    @Column(length = 500, nullable = true)
    private String content;

    @Column(nullable = false)
    private Integer rating;

    @Builder(access = AccessLevel.PRIVATE)
    private Review(Long skillExchangeId, Long revieweeSkillId, Long reviewerId,
                   Long revieweeId, String content, Integer rating) {
        this.skillExchangeId = skillExchangeId;
        this.revieweeSkillId = revieweeSkillId;
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.content = content;
        this.rating = rating;
    }

    // == 생성 메서드 ==
    public static Review create(Long skillExchangeId, Long revieweeSkillId, Long reviewerId,
                                Long revieweeId, String content, Integer rating) {
        return Review.builder()
                .skillExchangeId(skillExchangeId)
                .revieweeSkillId(revieweeSkillId)
                .reviewerId(reviewerId)
                .revieweeId(revieweeId)
                .content(content)
                .rating(rating)
                .build();
    }

    /**
     *  비즈니스 메서드
     */
    public void update(String newContent, Integer newRating){
        this.content = newContent;
        this.rating = newRating;
    }
}
