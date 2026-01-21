<<<<<<< HEAD
package org.swyp.linkit.domain.credit.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swyp.linkit.domain.user.entity.OAuthProvider;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.global.error.exception.InvalidCreditAmountException;
import org.swyp.linkit.global.error.exception.NotEnoughCreditException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreditTest {

    @Test
    @DisplayName("Credit 생성")
    public void create() {
        //given
        User user = createUser();
        int initialAmount = 0;

        //when
        Credit credit = Credit.create(user, initialAmount);

        //then
        assertThat(credit.getUser()).isEqualTo(user);
        assertThat(credit.getAmount()).isEqualTo(initialAmount);
    }

    @Test
    @DisplayName("Credit 차감_성공")
    public void decreaseAmount_success(){
        //given
        User user = createUser();
        int initialAmount = 5;
        int decrease = 5;
        Credit credit = Credit.create(user, initialAmount);

        //when
        credit.decreaseAmount(decrease);

        //then
        assertThat(credit.getAmount()).isEqualTo(initialAmount - decrease);
    }

    @Test
    @DisplayName("Credit 차감_실패_보유 잔액보다 많은 잔액 차감")
    public void decreaseAmount_fail_NotEnoughCreditException(){
        //given
        User user = createUser();
        int initialAmount = 5;
        int decrease = 6;
        Credit credit = Credit.create(user, initialAmount);

        //when && then
        assertThatThrownBy(() -> credit.decreaseAmount(decrease))
                .isInstanceOf(NotEnoughCreditException.class);
    }

    @Test
    @DisplayName("Credit 증가_성공")
    public void increaseAmount_success(){
        //given
        User user = createUser();
        int initialAmount = 0;
        int increase = 5;
        Credit credit = Credit.create(user, initialAmount);

        //when
        credit.increaseAmount(increase);

        //then
        assertThat(credit.getAmount()).isEqualTo(initialAmount + increase);
    }

    @Test
    @DisplayName("Credit 증가_실패_증가 금액이 음수")
    public void increaseAmount_fail_InvalidCreditAmountException(){
        //given
        User user = createUser();
        int initialAmount = 5;
        int increase = -10;
        Credit credit = Credit.create(user, initialAmount);

        //when && then
        assertThatThrownBy(() -> credit.increaseAmount(increase))
                .isInstanceOf(InvalidCreditAmountException.class);
    }


    private User createUser() {
        return User.create(
                OAuthProvider.KAKAO,
                "kakao_1",
                "test@test.com",
                "테스터",
                "tester");
    }

}
=======
//package org.swyp.linkit.domain.credit.entity;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.swyp.linkit.domain.user.entity.OAuthProvider;
//import org.swyp.linkit.domain.user.entity.User;
//import org.swyp.linkit.global.error.exception.InvalidCreditAmountException;
//import org.swyp.linkit.global.error.exception.NotEnoughCreditException;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//class CreditTest {
//
//    @Test
//    @DisplayName("Credit 생성")
//    public void create() {
//        //given
//        User user = createUser();
//        int initialAmount = 0;
//
//        //when
//        Credit credit = Credit.create(user, initialAmount);
//
//        //then
//        assertThat(credit.getUser()).isEqualTo(user);
//        assertThat(credit.getAmount()).isEqualTo(initialAmount);
//    }
//
//    @Test
//    @DisplayName("Credit 차감_성공")
//    public void decreaseAmount_success(){
//        //given
//        User user = createUser();
//        int initialAmount = 5;
//        int decrease = 5;
//        Credit credit = Credit.create(user, initialAmount);
//
//        //when
//        credit.decreaseAmount(decrease);
//
//        //then
//        assertThat(credit.getAmount()).isEqualTo(initialAmount - decrease);
//    }
//
//    @Test
//    @DisplayName("Credit 차감_실패_보유 잔액보다 많은 잔액 차감")
//    public void decreaseAmount_fail_NotEnoughCreditException(){
//        //given
//        User user = createUser();
//        int initialAmount = 5;
//        int decrease = 6;
//        Credit credit = Credit.create(user, initialAmount);
//
//        //when && then
//        assertThatThrownBy(() -> credit.decreaseAmount(decrease))
//                .isInstanceOf(NotEnoughCreditException.class);
//    }
//
//    @Test
//    @DisplayName("Credit 증가_성공")
//    public void increaseAmount_success(){
//        //given
//        User user = createUser();
//        int initialAmount = 0;
//        int increase = 5;
//        Credit credit = Credit.create(user, initialAmount);
//
//        //when
//        credit.increaseAmount(increase);
//
//        //then
//        assertThat(credit.getAmount()).isEqualTo(initialAmount + increase);
//    }
//
//    @Test
//    @DisplayName("Credit 증가_실패_증가 금액이 음수")
//    public void increaseAmount_fail_InvalidCreditAmountException(){
//        //given
//        User user = createUser();
//        int initialAmount = 5;
//        int increase = -10;
//        Credit credit = Credit.create(user, initialAmount);
//
//        //when && then
//        assertThatThrownBy(() -> credit.increaseAmount(increase))
//                .isInstanceOf(InvalidCreditAmountException.class);
//    }
//
//
//    private User createUser() {
//        return User.create(
//                OAuthProvider.KAKAO,
//                "kakao_1",
//                "test@test.com",
//                "테스터",
//                "tester");
//    }
//
//}
>>>>>>> develop
