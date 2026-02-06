package org.swyp.linkit.domain.settlement.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.swyp.linkit.domain.credit.service.CreditService;
import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.settlement.entity.Settlement;
import org.swyp.linkit.domain.settlement.entity.SettlementStatus;
import org.swyp.linkit.domain.settlement.repository.SettlementRepository;
import org.swyp.linkit.domain.user.entity.*;
import org.swyp.linkit.global.error.exception.InvalidSettlementStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("정산 처리 단위 테스트")
class SettlementProcessorTest {

    @Mock
    CreditService creditService;

    @Mock
    SettlementRepository settlementRepository;

    @InjectMocks
    SettlementProcessor settlementProcessor;


    @Test
    @DisplayName("정산 처리 대상의 정산을 completed로 변경, 크레딧 지급, receiver의 가르친 횟수를 증가시킨다.")
    public void processSingleSettlement_success() {
        // given
        User requester = createUser();
        User receiver = createUser();
        UserProfile receiverProfile = createProfile(receiver);
        UserSkill receiverSkill = createUserSkill();
        receiverProfile.addUserSkill(receiverSkill);

        SkillExchange exchange = createExchange(requester, receiver, receiverSkill);
        exchange.updateExchangeStatus(ExchangeStatus.ACCEPTED);
        Settlement settlement = createSettlement(exchange, receiver);

        Long settlementId = settlement.getId();
        int timesTaught = receiverProfile.getTimesTaught();

        // settlement 조회 Mock 처리
        when(settlementRepository.findByIdForSettlement(settlementId)).thenReturn(Optional.of(settlement));

        // when
        settlementProcessor.processSingleSettlement(settlementId);

        // then
        // settlement "PENDING" -> "COMPLETED"
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
        // receiver 가르친 횟수
        assertThat(receiverProfile.getTimesTaught()).isEqualTo(timesTaught + 1);
        verify(creditService, atMost(1)).settleCredit(settlement);
    }

    @Test
    @DisplayName("정산 처리 대상의 정산 상태가 PENDING이 아니면 InvalidSettlementStatusException")
    public void processSingleSettlement_fail_InvalidSettlementStatusException() {
        // given
        User requester = createUser();
        User receiver = createUser();
        UserProfile receiverProfile = createProfile(receiver);
        UserSkill receiverSkill = createUserSkill();
        receiverProfile.addUserSkill(receiverSkill);

        SkillExchange exchange = createExchange(requester, receiver, receiverSkill);
        Settlement settlement = createSettlement(exchange, receiver);
        Long settlementId = settlement.getId();

        // cancel로 변경
        settlement.cancel();

        // settlement 조회 Mock 처리
        when(settlementRepository.findByIdForSettlement(settlementId)).thenReturn(Optional.of(settlement));

        // when && then
        assertThatThrownBy(() -> settlementProcessor.processSingleSettlement(settlementId))
                .isInstanceOf(InvalidSettlementStatusException.class);
    }

    private UserProfile createProfile(User user){
        return UserProfile.create(
                user,
                "description",
                ExchangeType.OFFLINE,
                null,
                null
        );
    }

    private UserSkill createUserSkill(){
        SkillCategory skillCategory = SkillCategory.create(SkillCategoryType.DEVELOPMENT);
        return UserSkill.create(
                skillCategory,
                "name",
                "title",
                SkillProficiency.LOW,
                "description",
                30);
    }

    private Settlement createSettlement(SkillExchange exchange, User receiver){
        return Settlement.create(exchange, receiver);
    }

    private SkillExchange createExchange(User requester, User receiver, UserSkill receiverSKill){
        return SkillExchange.create(
                requester,
                receiver,
                receiverSKill,
                LocalDate.now(),
                LocalTime.now().minusHours(2),
                LocalTime.now().minusHours(1),
                "defaultMessage");
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