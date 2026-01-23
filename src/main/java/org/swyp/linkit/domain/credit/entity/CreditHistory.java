package org.swyp.linkit.domain.credit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
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

    // 내역 주인
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 상대방 (리워드 지급 시 null)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = true)
    private User targetUser;

    /**
     * 스킬 교환 (리워드 지급 시 null)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_exchange_id", nullable = true)
    private SkillExchange skillExchange;

    /**
     * 내역 명 (스킬명 혹은 리워드명)
     */
    @Column(nullable = false)
    private String contentName;

    // ADD, USE
    @Enumerated(EnumType.STRING)
    private SupplyType supplyType;

    // 변동 크레딧 (+ / -)
    @Column(nullable = false)
    private int changeAmount;

    // 거래 후 잔액
    @Column(nullable = false)
    private int balanceAfter;

    // 거래 타입
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HistoryType historyType;

    @Builder(access = AccessLevel.PRIVATE)
    public CreditHistory(SupplyType supplyType, String contentName, int changeAmount,
                         int balanceAfter, HistoryType historyType) {
        this.supplyType = supplyType;
        this.contentName = contentName;
        this.changeAmount = changeAmount;
        this.balanceAfter = balanceAfter;
        this.historyType = historyType;
    }

    // == 생성 메서드 ==

    /**
     *  리워드 생성 내역
     */
    public static CreditHistory createReward(User user, int amount, int balanceAfter, HistoryType historyType) {
        CreditHistory creditHistory = CreditHistory.builder()
                .supplyType(SupplyType.ADD)
                .contentName(historyType.getContentName())
                .changeAmount(amount)
                .balanceAfter(balanceAfter)
                .historyType(historyType)
                .build();

        // User 연관관계 주입
        creditHistory.assignUser(user);
        return creditHistory;
    }

    /**
     *  스킬 교환으로 인한 내역 생성
     */
    public static CreditHistory createSkillExchange(User user, User targetUser, SkillExchange skillExchange,
                                                    SupplyType supplyType, int amount, int balanceAfter,
                                                    HistoryType historyType) {
        CreditHistory creditHistory = CreditHistory.builder()
                .supplyType(supplyType)
                .contentName(skillExchange.getSkillName())
                .changeAmount(amount)
                .balanceAfter(balanceAfter)
                .historyType(historyType)
                .build();
        // User 연관관계 주입
        creditHistory.assignUser(user);
        // targetUser 연관관계 주입
        creditHistory.assignTargetUser(targetUser);
        // SkillExchange 연관관계 주입
        creditHistory.assignSkillExchange(skillExchange);

        return creditHistory;
    }

    // == User와 연관관계 설정 ==
    private void assignUser(User user) {
        this.user = user;
    }

    // == targetUser와 연관관계 설정 ==
    private void assignTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    // == SkillExchange와 연관관계 설정 ==
    private void assignSkillExchange(SkillExchange skillExchange) {
        this.skillExchange = skillExchange;
    }
}
