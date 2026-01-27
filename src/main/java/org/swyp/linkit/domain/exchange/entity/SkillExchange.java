package org.swyp.linkit.domain.exchange.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SkillExchange extends BaseTimeEntity {

    private static final int CREDIT_EXCHANGE_RATE_MINUTES = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_exchange_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_skill_id", nullable = false)
    private UserSkill receiverSkill;

    @Column(nullable = false)
    private String skillName;

    @Column(nullable = false)
    private int exchangeDuration;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private LocalDateTime requestDeadLine;

    @Column(nullable = false)
    private int creditPrice;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExchangeStatus exchangeStatus;

    private String message;

    @Builder(access = AccessLevel.PRIVATE)
    private SkillExchange(String skillName, int exchangeDuration, LocalDate scheduledDate,
                          LocalTime startTime, LocalTime endTime, LocalDateTime requestDeadLine,
                          int creditPrice, ExchangeStatus exchangeStatus, String message) {
        this.skillName = skillName;
        this.exchangeDuration = exchangeDuration;
        this.scheduledDate = scheduledDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requestDeadLine = requestDeadLine;
        this.creditPrice = creditPrice;
        this.exchangeStatus = exchangeStatus;
        this.message = message;
    }

    // == 생성 메서드 ==
    public static SkillExchange create(User requesterUser, User receiverUser, UserSkill receiverSkill,
                                       LocalDate scheduledDate, LocalTime startTime, LocalTime endTime,
                                       String message) {
        // 요청 마감 계산
        LocalDateTime calculateDeadLine = scheduledDate.atStartOfDay();
        // 시간으로 크레딧 가격 계산
        int price = receiverSkill.getExchangeDuration() / CREDIT_EXCHANGE_RATE_MINUTES;

        SkillExchange skillExchange = SkillExchange.builder()
                .skillName(receiverSkill.getSkillName())
                .exchangeDuration(receiverSkill.getExchangeDuration())
                .scheduledDate(scheduledDate)
                .startTime(startTime)
                .endTime(endTime)
                .requestDeadLine(calculateDeadLine)
                .creditPrice(price)
                .exchangeStatus(ExchangeStatus.PENDING)
                .message(message)
                .build();

        // == requesterUser 연관관계 주입 ==
        skillExchange.assignRequesterUser(requesterUser);
        // == receiverUser 연관관계 주입 ==
        skillExchange.assignReceiverUser(receiverUser);
        // == receiverSkill 연관관계 주입 ==
        skillExchange.assignReceiverSkill(receiverSkill);
        return skillExchange;
    }

    /**
     * 연관관계 메서드
     */
    // == requesterUser와 연관관계 설정 ==
    private void assignRequesterUser(User requesterUser) {
        this.requester = requesterUser;
    }

    // == receiverUser와 연관관계 설정 ==
    private void assignReceiverUser(User receiverUser) {
        this.receiver = receiverUser;
    }

    // == receiverSkill와 연관관계 설정 ==
    private void assignReceiverSkill(UserSkill receiverSkill) {
        this.receiverSkill = receiverSkill;
    }

    /**
     * 비즈니스 메서드
     */
    // == ExchangeStatus 변경 임시 메서드 ==
    public void updateExchangeStatus(ExchangeStatus exchangeStatus) {
        this.exchangeStatus = exchangeStatus;
    }

}
