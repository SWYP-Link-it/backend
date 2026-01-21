<<<<<<< HEAD
package org.swyp.linkit.domain.credit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.swyp.linkit.domain.credit.dto.CreditHistoryDto;
import org.swyp.linkit.domain.credit.entity.CreditHistory;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.repository.CreditHistoryRepository;
import org.swyp.linkit.domain.user.entity.OAuthProvider;
import org.swyp.linkit.domain.user.entity.User;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditHistoryServiceImplTest {

    @Mock
    private CreditHistoryRepository historyRepository;

    @InjectMocks
    private CreditHistoryServiceImpl historyService;

    private User user;
    @BeforeEach
    void setUp(){
        user = createSavedUser();
    }

    @Test
    @DisplayName("CreditHistory 생성")
    public void createCreditHistory(){
        //given
        int changeAmount = 2;
        int balanceAfter = 4;
        HistoryType historyType = HistoryType.SIGNUP_REWARD;
        CreditHistory history = createSavedRewardHistory(user, changeAmount, balanceAfter, historyType);

        // CreditHistory 저장 Mock
        when(historyRepository.save((any(CreditHistory.class)))).thenReturn(history);

        //when
        CreditHistoryDto result = historyService.createRewardHistory(user, changeAmount, balanceAfter, historyType);

        //then
        assertThat(result.getId()).isEqualTo(history.getId());
        assertThat(result.getUserId()).isEqualTo(history.getUser().getId());
        assertThat(result.getChangeAmount()).isEqualTo(history.getChangeAmount());
        assertThat(result.getBalanceAfter()).isEqualTo(history.getBalanceAfter());
        assertThat(result.getHistoryType()).isEqualTo(history.getHistoryType());
        assertThat(result.getSkillExchangeId()).isEqualTo(history.getSkillExchangeId());
        assertThat(result.getTargetUserId()).isEqualTo(history.getTargetUserId());
    }

    private User createSavedUser() {
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_1",
                "test@test.com",
                "테스터",
                "tester");

        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private CreditHistory createSavedRewardHistory(User user, int changeAmount, int balanceAfter, HistoryType historyType){
        CreditHistory history = CreditHistory.createReward(user, changeAmount, balanceAfter, historyType);
        ReflectionTestUtils.setField(history, "id", 10L);
        return history;
    }
}
=======
//package org.swyp.linkit.domain.credit.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.swyp.linkit.domain.credit.dto.CreditHistoryDto;
//import org.swyp.linkit.domain.credit.entity.CreditHistory;
//import org.swyp.linkit.domain.credit.entity.HistoryType;
//import org.swyp.linkit.domain.credit.repository.CreditHistoryRepository;
//import org.swyp.linkit.domain.user.entity.OAuthProvider;
//import org.swyp.linkit.domain.user.entity.User;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class CreditHistoryServiceImplTest {
//
//    @Mock
//    private CreditHistoryRepository historyRepository;
//
//    @InjectMocks
//    private CreditHistoryServiceImpl historyService;
//
//    private User user;
//    @BeforeEach
//    void setUp(){
//        user = createSavedUser();
//    }
//
//    @Test
//    @DisplayName("CreditHistory 생성")
//    public void createCreditHistory(){
//        //given
//        int changeAmount = 2;
//        int balanceAfter = 4;
//        HistoryType historyType = HistoryType.SIGNUP_REWARD;
//        CreditHistory history = createSavedRewardHistory(user, changeAmount, balanceAfter, historyType);
//
//        // CreditHistory 저장 Mock
//        when(historyRepository.save((any(CreditHistory.class)))).thenReturn(history);
//
//        //when
//        CreditHistoryDto result = historyService.createRewardHistory(user, changeAmount, balanceAfter, historyType);
//
//        //then
//        assertThat(result.getId()).isEqualTo(history.getId());
//        assertThat(result.getUserId()).isEqualTo(history.getUser().getId());
//        assertThat(result.getChangeAmount()).isEqualTo(history.getChangeAmount());
//        assertThat(result.getBalanceAfter()).isEqualTo(history.getBalanceAfter());
//        assertThat(result.getHistoryType()).isEqualTo(history.getHistoryType());
//        assertThat(result.getSkillExchangeId()).isEqualTo(history.getSkillExchangeId());
//        assertThat(result.getTargetUserId()).isEqualTo(history.getTargetUserId());
//    }
//
//    private User createSavedUser() {
//        User user = User.create(
//                OAuthProvider.KAKAO,
//                "kakao_1",
//                "test@test.com",
//                "테스터",
//                "tester");
//
//        ReflectionTestUtils.setField(user, "id", 1L);
//        return user;
//    }
//
//    private CreditHistory createSavedRewardHistory(User user, int changeAmount, int balanceAfter, HistoryType historyType){
//        CreditHistory history = CreditHistory.createReward(user, changeAmount, balanceAfter, historyType);
//        ReflectionTestUtils.setField(history, "id", 10L);
//        return history;
//    }
//}
>>>>>>> develop
