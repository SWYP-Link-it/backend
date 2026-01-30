package org.swyp.linkit.domain.exchange.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;
import org.swyp.linkit.global.error.exception.InvalidExchangeStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SkillExchange extends BaseTimeEntity {

    public static final int CREDIT_EXCHANGE_RATE_MINUTES = 30;

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

    @Column(nullable = false)
    private boolean isRequesterRead;

    @Column(nullable = false)
    private boolean isReceiverRead;

    @Column(length = 50)
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
        this.isReceiverRead = false;
        this.isRequesterRead = false;
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
    // == ExchangeStatus 수락 처리 ==
    public void accept(){
        if(!this.exchangeStatus.equals(ExchangeStatus.PENDING)){
            throw new InvalidExchangeStatusException("수락은 대기중 상태의 거래만 가능합니다.");
        }
        this.exchangeStatus = ExchangeStatus.ACCEPTED;
    }

    // == ExchangeStatus 거절 처리 ==
    public void reject(){
        if(!this.exchangeStatus.equals(ExchangeStatus.PENDING)){
            throw new InvalidExchangeStatusException("거절은 대기중 상태의 거래만 가능합니다.");
        }
        this.exchangeStatus = ExchangeStatus.REJECTED;
    }

    // == ExchangeStatus 만료 처리 ==
    public void expire(){
        // expire 대상은 DB조회에서 걸러지지만 혹시모를 상황 대비
        if(!this.exchangeStatus.equals(ExchangeStatus.PENDING)){
            return;
        }
        this.exchangeStatus = ExchangeStatus.EXPIRED;
    }

    // == ExchangeStatus 취소 처리 ==
    public void cancel(){
        this.exchangeStatus = ExchangeStatus.CANCELED;
    }

    // == isRequesterRead false 처리 --
    public void updateRequesterReadToFalse(){
        this.isRequesterRead = false;
    }

    // == isReceiverRead false 처리 --
    public void updateReceiverReadToFalse(){
        this.isReceiverRead = false;
    }

    // == isRequesterRead true 처리 --
    public void updateRequesterReadToTrue(){
        this.isRequesterRead = true;
    }

    // == isReceiverRead true 처리 --
    public void updateReceiverReadToTrue(){
        this.isReceiverRead = true;
    }

    // == ExchangeStatus 변경 메서드 ==
    public void updateExchangeStatus(ExchangeStatus exchangeStatus) {
        this.exchangeStatus = exchangeStatus;
    }

}