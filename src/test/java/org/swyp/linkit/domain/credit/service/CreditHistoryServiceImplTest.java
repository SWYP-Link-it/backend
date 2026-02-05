package org.swyp.linkit.domain.credit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.swyp.linkit.domain.credit.dto.RewardHistoryDto;
import org.swyp.linkit.domain.credit.entity.CreditHistory;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.entity.SupplyType;
import org.swyp.linkit.domain.credit.repository.CreditHistoryRepository;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.user.entity.*;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditHistoryServiceImplTest {

    @Mock
    private CreditHistoryRepository historyRepository;

    @InjectMocks
    private CreditHistoryServiceImpl historyService;

    private User user;
    private Long historyId = 1L;
    private Long userId = 1L;
    private Long skillExchangeId = 1L;

    @BeforeEach
    void setUp(){
        user = createUser();
    }

    @Test
    @DisplayName("크레딧 리워드 내역 생성")
    public void createRewardHistory(){
        //given
        int changeAmount = 2;
        int balanceAfter = 4;
        HistoryType historyType = HistoryType.SIGNUP_REWARD;
        CreditHistory history = createRewardHistory(user, changeAmount, balanceAfter, historyType);

        // CreditHistory 저장 Mock
        when(historyRepository.save((any(CreditHistory.class)))).thenReturn(history);

        //when
        RewardHistoryDto sut = historyService.createRewardHistory(user, changeAmount, balanceAfter, historyType);

        //then
        assertThat(sut.getHistoryId()).isEqualTo(history.getId());
        assertThat(sut.getUserId()).isEqualTo(history.getUser().getId());
        assertThat(sut.getChangeAmount()).isEqualTo(history.getChangeAmount());
        assertThat(sut.getBalanceAfter()).isEqualTo(history.getBalanceAfter());
        assertThat(sut.getHistoryType()).isEqualTo(history.getHistoryType());
        assertThat(sut.getSupplyType()).isEqualTo(history.getSupplyType());
        assertThat(sut.getContentName()).isEqualTo(history.getContentName());
    }

    @Test
    @DisplayName("크레딧 스킬 교환 내역 생성")
    public void createExchangeHistory() {
        // given
        User requester = user;
        User receiver = createUser();
        UserSkill userSkill = createUserSkill();
        SupplyType supplyType = SupplyType.USE;
        int amount = 2;
        int balanceAfter = 4;
        HistoryType historyType = HistoryType.EXCHANGE_REQUEST;

        SkillExchange skillExchange = createSkillExchange(requester, receiver, userSkill);

        CreditHistory exchangeHistory = createExchangeHistory(requester, receiver, skillExchange,
                supplyType, amount, balanceAfter, historyType);

        when(historyRepository.save((any(CreditHistory.class)))).thenReturn(exchangeHistory);

        // when
        CreditHistory sut = historyService
                .createExchangeHistory(requester, receiver, skillExchange, supplyType, amount, balanceAfter, historyType);

        // then
        assertThat(sut.getUser()).isEqualTo(requester);
        assertThat(sut.getTargetUser()).isEqualTo(receiver);
        assertThat(sut.getSkillExchange()).isEqualTo(skillExchange);
        assertThat(sut.getContentName()).isEqualTo(userSkill.getSkillName());
        assertThat(sut.getSupplyType()).isEqualTo(supplyType);
        assertThat(sut.getChangeAmount()).isEqualTo(amount);
        assertThat(sut.getHistoryType()).isEqualTo(historyType);
    }



    private UserSkill createUserSkill(){
        SkillCategory skillCategory = SkillCategory.create(SkillCategoryType.DEVELOPMENT);
        UserSkill userSkill = UserSkill.create(skillCategory, "JAVA", "TITLE",
                SkillProficiency.HIGH, "description", 30);
        ReflectionTestUtils.setField(userSkill, "id", 1L);
        return userSkill;
    }

    private SkillExchange createSkillExchange(User requester, User receiver, UserSkill userSkill){

        SkillExchange skillExchange = SkillExchange.create(
                requester,
                receiver,
                userSkill,
                LocalDate.of(2026, 1, 31),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                "message");
        ReflectionTestUtils.setField(skillExchange, "id", skillExchangeId++);
        return skillExchange;
    }

    private User createUser() {
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_1",
                "test@test.com",
                "테스터",
                "https://image",
                "tester");

        ReflectionTestUtils.setField(user, "id", userId++);
        return user;
    }

    private CreditHistory createRewardHistory(User user, int changeAmount, int balanceAfter, HistoryType historyType){
        CreditHistory history = CreditHistory.createReward(user, changeAmount, balanceAfter, historyType);
        ReflectionTestUtils.setField(history, "id", historyId++);
        return history;
    }

    private CreditHistory createExchangeHistory(User user, User target, SkillExchange skillExchange,
                                                SupplyType supplyType, int amount, int balanceAfter, HistoryType historyType){
        CreditHistory history = CreditHistory.createSkillExchange(user, target, skillExchange,
                supplyType, amount, balanceAfter, historyType);
        ReflectionTestUtils.setField(history, "id", historyId++);
        return history;
    }
}