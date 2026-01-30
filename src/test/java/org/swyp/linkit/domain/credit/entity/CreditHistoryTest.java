package org.swyp.linkit.domain.credit.entity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.user.entity.OAuthProvider;
import org.swyp.linkit.domain.user.entity.SkillProficiency;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserSkill;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class CreditHistoryTest {
    static User user;
    private int amount = 2;
    private int balanceAfter = 4;
    static Long userId = 1L;

    @BeforeAll
    static void setup(){
        user = createUser(userId++);
    }

    @Test
    @DisplayName("크레딧 리워드 내역 생성")
    public void createReward() {
        // given
        HistoryType historyType = HistoryType.SIGNUP_REWARD;
        SupplyType supplyType = SupplyType.ADD;

        // when
        CreditHistory sut = CreditHistory.createReward(user, amount, balanceAfter, historyType);

        // then
        assertThat(sut.getUser()).isEqualTo(user);
        assertThat(sut.getSupplyType()).isEqualTo(supplyType);
        assertThat(sut.getContentName()).isEqualTo(historyType.getContentName());
        assertThat(sut.getChangeAmount()).isEqualTo(amount);
        assertThat(sut.getBalanceAfter()).isEqualTo(balanceAfter);
        assertThat(sut.getHistoryType()).isEqualTo(historyType);

        // targetUser, skillExchange 는 null 이어야한다.
        assertThat(sut.getTargetUser()).isNull();
        assertThat(sut.getSkillExchange()).isNull();
    }

    @Test
    @DisplayName("크레딧 스킬 거래 내역 생성")
    public void createSkillExchange() {
        // given
        User targetUser = createUser(userId++);
        HistoryType historyType = HistoryType.EXCHANGE_REQUEST;
        SupplyType supplyType = SupplyType.USE;

        UserSkill userSkill = UserSkill.create(null, "name", "title",
                SkillProficiency.HIGH, "description", 30);
        SkillExchange skillExchange = SkillExchange
                .create(user, targetUser, userSkill, LocalDate.of(2026, 1, 31),
                        LocalTime.of(10, 0), LocalTime.of(12, 0), "message");

        // when
        CreditHistory sut = CreditHistory
                .createSkillExchange(user, targetUser, skillExchange, SupplyType.USE,
                        amount, balanceAfter, HistoryType.EXCHANGE_REQUEST);

        // then
        assertThat(sut.getUser()).isEqualTo(user);
        assertThat(sut.getSupplyType()).isEqualTo(supplyType);
        assertThat(sut.getContentName()).isEqualTo(userSkill.getSkillName());
        assertThat(sut.getChangeAmount()).isEqualTo(amount);
        assertThat(sut.getBalanceAfter()).isEqualTo(balanceAfter);
        assertThat(sut.getHistoryType()).isEqualTo(historyType);
        assertThat(sut.getTargetUser()).isEqualTo(targetUser);
        assertThat(sut.getSkillExchange()).isEqualTo(skillExchange);
    }

    private static User createUser(Long userId) {
        return User.create(
                OAuthProvider.KAKAO,
                "kakao" + userId,
                "email@example.com" + userId,
                "name" + userId,
                "https://image-examle" + userId,
                "nickname" + userId);
    }
}