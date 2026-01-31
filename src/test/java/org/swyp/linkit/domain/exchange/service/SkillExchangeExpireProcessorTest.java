package org.swyp.linkit.domain.exchange.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.service.CreditService;
import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.user.entity.OAuthProvider;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserSkill;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("만료된 스킬 거래 처리 단위 테스트")
class SkillExchangeExpireProcessorTest {

    @Mock
    CreditService creditService;

    @InjectMocks
    SkillExchangeExpireProcessor exchangeExpireProcessor;

    @Test
    @DisplayName("만료된 스킬 거래의 상태를 expired로 변경하고 receiver, requester의 알림을 초기화한다.")
    public void expireSingleSkillExchange() {
        // given
        User requester = createUser();
        User receiver = createUser();
        UserSkill userSkill = UserSkill
                .create(null, "name", "titme", null, null, 30);
        LocalTime start = LocalTime.now();

        // 알림 초기 상태는 false 이기에 true로 변경
        SkillExchange skillExchange = SkillExchange.create(
                requester,
                receiver,
                userSkill,
                LocalDate.now(),
                start,
                start.plusHours(1),
                null);

        // when
        doNothing().when(creditService).refundCreditForExchange(skillExchange, HistoryType.EXCHANGE_EXPIRED);
        exchangeExpireProcessor.expireSingleSkillExchange(skillExchange);

        // then
        assertThat(skillExchange.getExchangeStatus()).isEqualTo(ExchangeStatus.EXPIRED);
        assertThat(skillExchange.isRequesterRead()).isFalse();
        assertThat(skillExchange.isReceiverRead()).isFalse();
        verify(creditService, times(1)).refundCreditForExchange(skillExchange, HistoryType.EXCHANGE_EXPIRED);
    }

    private User createUser() {
        return User.create(
                OAuthProvider.KAKAO,
                "kakao",
                "email@example.com",
                "name",
                "https://image",
                "nickname");
    }
}