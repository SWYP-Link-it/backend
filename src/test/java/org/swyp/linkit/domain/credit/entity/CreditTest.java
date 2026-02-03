package org.swyp.linkit.domain.credit.entity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.swyp.linkit.domain.user.entity.OAuthProvider;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.global.error.exception.InvalidCreditAmountException;
import org.swyp.linkit.global.error.exception.NotEnoughCreditException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreditTest {
    static User user;
    private int baseBalance = 2;

    @BeforeAll
    static void setup(){
        user = createUser();
    }

    @Test
    @DisplayName("크레딧 생성")
    public void create() {
        // when
        Credit sut = Credit.create(user, baseBalance);

        // then
        assertThat(sut.getUser()).isEqualTo(user);
        assertThat(sut.getBalance()).isEqualTo(baseBalance);
    }

    @Nested
    @DisplayName("크레딧 사용")
    class UseCredit {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {

            @Test
            @DisplayName("크레딧 사용으로 인해 크레딧 감소")
            public void success() {
                // given
                Credit credit = Credit.create(user, baseBalance);
                int balance = credit.getBalance();
                int amount = 1;
                int balanceAfter = balance - amount;

                // when
                credit.useCredit(amount);

                // then
                assertThat(credit.getBalance()).isEqualTo(balanceAfter);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("크레딧 사용 시 보유량보다 사용량이 커서 예외 발생 NotEnoughCreditException")
            public void fail_NotEnoughCreditException() {
                // given
                Credit credit = Credit.create(user, baseBalance);
                int balance = credit.getBalance();
                int amount = balance + 1;

                // when && then
                assertThatThrownBy(() -> credit.useCredit(amount))
                        .isInstanceOf(NotEnoughCreditException.class);
            }
        }
    }

    @Nested
    @DisplayName("크레딧 지급")
    class addCredit {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {

            @Test
            @DisplayName("크레딧 지급으로 인해 크레딧 증가(리워드, 충전)")
            public void success() {
                // given
                Credit credit = Credit.create(user, baseBalance);
                int balance = credit.getBalance();
                int amount = 1;
                int afterBalance = balance + amount;

                // when
                credit.addCredit(amount);

                // then
                assertThat(credit.getBalance()).isEqualTo(afterBalance);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("크레딧 지급 시 음수로 인해 예외 발생 InvalidCreditAmountException")
            public void fail_InvalidCreditAmountException() {
                // given
                Credit credit = Credit.create(user, baseBalance);
                int balance = credit.getBalance();
                int amount = balance - (balance + 1);

                // when && then
                assertThatThrownBy(() -> credit.addCredit(amount))
                        .isInstanceOf(InvalidCreditAmountException.class);
            }
        }
    }

    private static User createUser() {
        return User.create(
                OAuthProvider.KAKAO,
                "kakao",
                "email@example.com",
                "name",
                "https://image",
                "nickname");
    }
}