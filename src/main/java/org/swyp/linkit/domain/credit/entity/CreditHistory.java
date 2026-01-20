package org.swyp.linkit.domain.credit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CreditHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credit_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int changeAmount;

    @Column(nullable = false)
    private int balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HistoryType historyType;

    // 크레딧 변동이 스킬 교환일 경우만 세팅
    private Long skillExchangeId;

    // 크레딧 변동이 스킬 교환일 경우만 세팅
    private Long targetUserId;

    @Builder(access = AccessLevel.PRIVATE)
    private CreditHistory(int changeAmount, int balanceAfter, HistoryType historyType,
                          Long skillExchangeId, Long targetUserId) {
        this.changeAmount = changeAmount;
        this.balanceAfter = balanceAfter;
        this.historyType = historyType;
        this.skillExchangeId = skillExchangeId;
        this.targetUserId = targetUserId;
    }

    // == 생성 메서드 ==
    public static CreditHistory createReward(User user, int changeAmount,
                                             int balanceAfter, HistoryType historyType) {
        CreditHistory creditHistory = CreditHistory.builder()
                .changeAmount(changeAmount)
                .balanceAfter(balanceAfter)
                .historyType(historyType)
                .build();
        // User 연관관계 주입
        creditHistory.assignUser(user);
        return creditHistory;
    }

    public static CreditHistory createSkillExchange(User user, int changeAmount, int balanceAfter,
                                                    HistoryType historyType, Long skillExchangeId,
                                                    Long targetUserId) {
        CreditHistory creditHistory = CreditHistory.builder()
                .changeAmount(changeAmount)
                .balanceAfter(balanceAfter)
                .historyType(historyType)
                .skillExchangeId(skillExchangeId)
                .targetUserId(targetUserId)
                .build();
        // User 연관관계 주입
        creditHistory.assignUser(user);
        return creditHistory;
    }

    // == User와 연관관계 설정 ==
    private void assignUser(User user) {
        this.user = user;
    }
}
