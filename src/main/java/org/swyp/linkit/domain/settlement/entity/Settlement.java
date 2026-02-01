package org.swyp.linkit.domain.settlement.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;
import org.swyp.linkit.global.error.exception.InvalidSettlementStatusException;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Settlement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_exchange_id", nullable = false)
    private SkillExchange skillExchange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    @Column(nullable = true)
    private LocalDateTime settledAt;

    @Builder(access = AccessLevel.PRIVATE)
    public Settlement(SkillExchange skillExchange, User receiver, int amount) {
        this.skillExchange = skillExchange;
        this.receiver = receiver;
        this.amount = amount;
        this.status = SettlementStatus.PENDING;
    }

    // == 생성 메서드 ==
    public static Settlement create(SkillExchange skillExchange, User receiver) {
        return Settlement.builder()
                .skillExchange(skillExchange)
                .receiver(receiver)
                .amount(skillExchange.getCreditPrice())
                .build();
    }

    /**
     *  비즈니스 메서드
     */
    // 정산 완료 처리
    public void complete(){
        if(!this.status.equals(SettlementStatus.PENDING)){
            throw new InvalidSettlementStatusException("대기 중인 정산만 완료 처리할 수 있습니다.");
        }
        this.status = SettlementStatus.COMPLETED;
        this.settledAt = LocalDateTime.now();
    }

    // 정산 취소 처리 (거래 수락 후 거래가 취소 되었을 경우)
    public void cancel(){
        if(!this.status.equals(SettlementStatus.PENDING)){
            throw new InvalidSettlementStatusException("대기 중인 정산만 취소할 수 있습니다.");
        }
        this.status = SettlementStatus.CANCELED;
    }
}
