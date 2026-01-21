package org.swyp.linkit.domain.credit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;
import org.swyp.linkit.global.error.exception.InvalidCreditAmountException;
import org.swyp.linkit.global.error.exception.NotEnoughCreditException;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Credit extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credit_id")
    private Long id;

    // 크레딧 보유량
    @Column(nullable = false)
    private int balance;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private Credit(int balance) {
        this.balance = balance;
    }

    // == 생성 메서드 ==
    public static Credit create(User user, int amount) {
        Credit credit = Credit.builder()
                .balance(amount)
                .build();
        // User 연관관계 주입
        credit.assignUser(user);
        return credit;
    }

    // == User와 연관관계 설정 ==
    private void assignUser(User user) {
        this.user = user;
    }

    // ====== 비즈니스 메서드 ======

    // 크레딧 차감
    public void decreaseAmount(int amount) {
        if (this.balance < amount) {
            throw new NotEnoughCreditException();
        }
        this.balance -= amount;
    }

    // 크레딧 증가 (리워드, 충전)
    public void increaseAmount(int amount) {
        if (amount < 0) {
            throw new InvalidCreditAmountException();
        }
        this.balance += amount;
    }
}
