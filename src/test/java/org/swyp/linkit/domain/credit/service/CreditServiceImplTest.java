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
//import org.swyp.linkit.domain.credit.dto.CreditDto;
//import org.swyp.linkit.domain.credit.entity.Credit;
//import org.swyp.linkit.domain.credit.entity.HistoryType;
//import org.swyp.linkit.domain.credit.repository.CreditRepository;
//import org.swyp.linkit.domain.user.entity.OAuthProvider;
//import org.swyp.linkit.domain.user.entity.User;
//import org.swyp.linkit.global.error.exception.NotFoundCreditException;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class CreditServiceImplTest {
//
//    @Mock
//    private CreditRepository creditRepository;
//
//    @Mock
//    private CreditHistoryService historyService;
//
//    @InjectMocks
//    private CreditServiceImpl creditService;
//
//    private User user;
//    private final int INITIAL_AMOUNT = 0;
//    private final int SIGNUP_REWARD = 2;
//    private final int PROFILE_REWARD = 1;
//
//    @BeforeEach
//    void setUp(){
//        user = createSavedUser();
//    }
//
//    @Test
//    @DisplayName("크레딧 생성")
//    public void createSavedCredit(){
//        //given
//        Credit credit = createSavedCredit(user);
//
//        // credit 저장 mock
//        when(creditRepository.save(any(Credit.class))).thenReturn(credit);
//
//        //when
//        CreditDto result = creditService.createCredit(user);
//
//        //then
//        assertThat(result.getAmount()).isEqualTo(INITIAL_AMOUNT);
//        assertThat(result.getUserId()).isEqualTo(user.getId());
//        assertThat(result.getId()).isEqualTo(credit.getId());
//    }
//
//    @Test
//    @DisplayName("회원가입 리워드 지급_성공")
//    public void rewardCreditOnSignupSetup_success(){
//        //given
//        HistoryType historyType = HistoryType.SIGNUP_REWARD;
//        Credit credit = createSavedCredit(user);
//
//        // credit 조회 mock
//        when(creditRepository.findByUserId(user.getId())).thenReturn(Optional.of(credit));
//
//        //when
//        CreditDto result = creditService.rewardCreditOnSignupSetup(user);
//
//        //then
//        assertThat(result.getAmount()).isEqualTo(INITIAL_AMOUNT + SIGNUP_REWARD);
//        verify(historyService, times(1)).createRewardHistory(
//                eq(user),
//                eq(SIGNUP_REWARD),
//                eq(INITIAL_AMOUNT + SIGNUP_REWARD),
//                eq(historyType)
//        );
//    }
//
//    @Test
//    @DisplayName("회원가입 리워드 지급_실패_크레딧이 존재 하지 않음")
//    public void rewardCreditOnSignupSetup_fail_NotFoundCreditException(){
//        //given
//        ReflectionTestUtils.setField(user, "id", 1L);
//
//        // credit 조회 mock
//        when(creditRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
//
//        //when && then
//        assertThatThrownBy(() -> creditService.rewardCreditOnSignupSetup(user)).isInstanceOf(NotFoundCreditException.class);
//    }
//
//    @Test
//    @DisplayName("프로필 리워드 지급_성공")
//    public void rewardCreditOnProfileSetup_success(){
//        //given
//        HistoryType historyType = HistoryType.PROFILE_REWARD;
//
//        Credit credit = createSavedCredit(user);
//
//        // credit 조회 mock
//        when(creditRepository.findByUserId(user.getId())).thenReturn(Optional.of(credit));
//
//        //when
//        CreditDto result = creditService.rewardCreditOnProfileSetup(user);
//
//        //then
//        assertThat(result.getAmount()).isEqualTo(INITIAL_AMOUNT + PROFILE_REWARD);
//        verify(historyService, times(1)).createRewardHistory(
//                eq(user),
//                eq(PROFILE_REWARD),
//                eq(INITIAL_AMOUNT + PROFILE_REWARD),
//                eq(historyType)
//        );
//    }
//
//    @Test
//    @DisplayName("프로필 리워드 지급_실패_크레딧이 존재 하지 않음")
//    public void rewardCreditOnProfileSetup_fail_NotFoundCreditException(){
//        //given
//        // credit 조회 mock
//        when(creditRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
//
//        //when && then
//        assertThatThrownBy(() -> creditService.rewardCreditOnProfileSetup(user)).isInstanceOf(NotFoundCreditException.class);
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
//    private Credit createSavedCredit(User user) {
//        Credit credit = Credit.create(user, INITIAL_AMOUNT);
//        ReflectionTestUtils.setField(credit, "id", 5L);
//        return credit;
//    }
//}
