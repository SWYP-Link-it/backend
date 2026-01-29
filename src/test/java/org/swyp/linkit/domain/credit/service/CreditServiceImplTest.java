package org.swyp.linkit.domain.credit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.swyp.linkit.domain.credit.dto.CreditDto;
import org.swyp.linkit.domain.credit.dto.CreditWithUserDetailsDto;
import org.swyp.linkit.domain.credit.entity.Credit;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.entity.SupplyType;
import org.swyp.linkit.domain.credit.repository.CreditRepository;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.user.entity.*;
import org.swyp.linkit.global.error.exception.NotEnoughCreditException;
import org.swyp.linkit.global.error.exception.NotFoundCreditException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditServiceImplTest {

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private CreditHistoryService historyService;

    @InjectMocks
    private CreditServiceImpl creditService;

    private Long userId = 1L;
    private User user;
    private Credit credit;

    @BeforeEach
    void setUp(){
        user = createUser();
        credit = Credit.create(user, 0);
    }

    @Test
    @DisplayName("크레딧 생성")
    public void createCredit() {
        // given
        when(creditRepository.save(any(Credit.class))).thenReturn(credit);

        // when
        CreditDto sut = creditService.createCredit(user);

        // then
        assertThat(sut.getBalance()).isEqualTo(credit.getBalance());
        assertThat(sut.getUserId()).isEqualTo(user.getId());
    }

    @Nested
    @DisplayName("회원가입 크레딧 지급")
    class RewardCreditOnSignupSetup {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            int amount = Credit.SIGNUP_REWARD;
            HistoryType historyType = HistoryType.SIGNUP_REWARD;

            @Test
            @DisplayName("성공")
            public void success() {
                // given
                int balance = credit.getBalance();
                Long userId = user.getId();
                // credit 조회 Mock 처리
                when(creditRepository.findByUserId(userId)).thenReturn(Optional.of(credit));

                // when
                CreditDto sut = creditService.rewardCreditOnSignupSetup(user);

                // then
                assertThat(sut.getCreditId()).isEqualTo(credit.getId());
                assertThat(sut.getBalance()).isEqualTo(balance + amount);
                assertThat(sut.getUserId()).isEqualTo(user.getId());
                verify(historyService).createRewardHistory(eq(user), eq(2), anyInt(), eq(historyType));
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("크레딧이 존재하지 않아 NotFoundCreditException")
            public void fail_NotFoundCreditException() {
                // given
                Long userId = user.getId();
                // credit 조회 Mock 처리
                when(creditRepository.findByUserId(userId)).thenReturn(Optional.empty());

                // when && then
                assertThatThrownBy(() -> creditService.rewardCreditOnSignupSetup(user))
                        .isInstanceOf(NotFoundCreditException.class);
            }
        }
    }

    @Nested
    @DisplayName("프로필 설정 완료 크레딧 지급")
    class RewardCreditOnProfileSetup {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            int amount = Credit.PROFILE_REWARD;
            HistoryType historyType = HistoryType.PROFILE_REWARD;

            @Test
            @DisplayName("성공")
            public void success() {
                // given
                int balance = credit.getBalance();
                Long userId = user.getId();
                // credit 조회 Mock 처리
                when(creditRepository.findByUserId(userId)).thenReturn(Optional.of(credit));

                // when
                CreditDto sut = creditService.rewardCreditOnProfileSetup(user);

                // then
                assertThat(sut.getCreditId()).isEqualTo(credit.getId());
                assertThat(sut.getBalance()).isEqualTo(balance + amount);
                assertThat(sut.getUserId()).isEqualTo(user.getId());
                verify(historyService).createRewardHistory(eq(user), eq(1), anyInt(), eq(historyType));
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("크레딧이 존재하지 않아 NotFoundCreditException")
            public void fail_NotFoundCreditException() {
                // given
                Long userId = user.getId();
                // credit 조회 Mock 처리
                when(creditRepository.findByUserId(userId)).thenReturn(Optional.empty());

                // when && then
                assertThatThrownBy(() -> creditService.rewardCreditOnProfileSetup(user))
                        .isInstanceOf(NotFoundCreditException.class);
            }
        }
    }

    @Nested
    @DisplayName("크레딧 잔액 조회")
    class GetCreditBalance {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("성공")
            public void success() {
                // given
                Long userId = user.getId();
                // credit 조회 Mock 처리
                when(creditRepository.findByUserId(userId)).thenReturn(Optional.of(credit));

                // when
                CreditDto sut = creditService.getCreditBalance(userId);

                // then
                assertThat(sut.getCreditId()).isEqualTo(credit.getId());
                assertThat(sut.getBalance()).isEqualTo(credit.getBalance());
                assertThat(sut.getUserId()).isEqualTo(user.getId());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("크레딧이 존재하지 않아 CreditNotFoundException")
            public void fail_CreditNotFoundException() {
                // given
                Long userId = user.getId();
                // credit 조회 Mock 처리
                when(creditRepository.findByUserId(userId)).thenReturn(Optional.empty());

                // when && then
                assertThatThrownBy(() -> creditService.getCreditBalance(userId))
                        .isInstanceOf(NotFoundCreditException.class);
            }
        }
    }

    @Nested
    @DisplayName("크레딧 잔액 조회 및 유저 정보 조회")
    class GetCreditBalanceWithUserDetails {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("성공")
            public void success() {
                // given
                int balance = credit.getBalance();
                Long userId = user.getId();
                // credit 조회 Mock 처리
                when(creditRepository.findByUserId(userId)).thenReturn(Optional.of(credit));

                // when
                CreditWithUserDetailsDto sut = creditService.getCreditBalanceWithUserDetails(userId);

                // then
                assertThat(sut.getBalance()).isEqualTo(balance);
                assertThat(sut.getCreditId()).isEqualTo(credit.getId());
                assertThat(sut.getUserId()).isEqualTo(user.getId());
                assertThat(sut.getUserNickname()).isEqualTo(user.getNickname());
                assertThat(sut.getUserProfileImageUrl()).isEqualTo(user.getProfileImageUrl());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("크레딧이 존재하지 않아 CreditNotFoundException")
            public void fail_CreditNotFoundException() {
                // given
                Long userId = user.getId();
                // credit 조회 Mock 처리
                when(creditRepository.findByUserId(userId)).thenReturn(Optional.empty());

                // when && then
                assertThatThrownBy(() -> creditService.getCreditBalanceWithUserDetails(userId))
                        .isInstanceOf(NotFoundCreditException.class);
            }
        }
    }

    @Nested
    @DisplayName("크레딧 사용 가능 여부 검증")
    class ValidateAvailableBalance {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("성공")
            public void success() {
                // given
                int balance = credit.getBalance();
                int amount = balance - 1;
                Long userId = user.getId();
                // credit 조회 Mock 처리
                when(creditRepository.findByUserId(userId)).thenReturn(Optional.of(credit));

                // when && then
                creditService.validateAvailableBalance(userId, amount);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("보유 크레딧보다 요청 크레디딧이 더 커서 NotEnoughCreditException")
            public void fail_NotEnoughCreditException() {
                // given
                int balance = credit.getBalance();
                int amount = balance + 1;
                Long userId = user.getId();
                // credit 조회 Mock 처리
                when(creditRepository.findByUserId(userId)).thenReturn(Optional.of(credit));

                // when && then
                assertThatThrownBy(() -> creditService.validateAvailableBalance(userId, amount))
                        .isInstanceOf(NotEnoughCreditException.class);
            }
        }
    }

    @Nested
    @DisplayName("크레딧 사용 - 스킬 교환 요청 (requester에게 credit 감소 및 creditHistory 생성)")
    class UseCreditForExchangeRequest {
        private User requester;
        private User receiver;
        private UserSkill receiverSkill;
        private SkillExchange skillExchange;

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("성공")
            public void success() {
                // given
                requester = user;
                receiver = createUser();
                UserSkill receiverSkill = createUserSkill();
                skillExchange = createSkillExchange(requester, receiver, receiverSkill);
                credit.addCredit(10);
                // credit 조회 Mock 처리
                when(creditRepository.findByUserId(requester.getId())).thenReturn(Optional.of(credit));
                System.out.print("balance = " +credit.getBalance());

                // when
                creditService.useCreditForExchangeRequest(skillExchange);

                // then
                verify(historyService).createExchangeHistory(
                        eq(requester),
                        eq(receiver),
                        eq(skillExchange),
                        eq(SupplyType.USE),
                        eq(skillExchange.getCreditPrice()),
                        eq(skillExchange.getCreditPrice() + credit.getBalance()),
                        eq(HistoryType.EXCHANGE_REQUEST));
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("크레딧이 존재하지 않아 CreditNotFoundException")
            public void fail_CreditNotFoundException() {
                // given
                requester = user;
                receiver = createUser();
                UserSkill receiverSkill = createUserSkill();
                skillExchange = createSkillExchange(requester, receiver, receiverSkill);
                Long userId = user.getId();
                // credit 조회 Mock 처리
                when(creditRepository.findByUserId(userId)).thenReturn(Optional.empty());

                // when && then
                assertThatThrownBy(() -> creditService.useCreditForExchangeRequest(skillExchange))
                        .isInstanceOf(NotFoundCreditException.class);
            }

            @Test
            @DisplayName("크레딧 보유량이 사용량 보다 적어 NotEnoughCreditException")
            public void fail_NotEnoughCreditException() {
                // given
                requester = user;
                receiver = createUser();
                UserSkill receiverSkill = createUserSkill();
                skillExchange = createSkillExchange(requester, receiver, receiverSkill);
                credit.useCredit(credit.getBalance());
                // credit 조회 Mock 처리
                when(creditRepository.findByUserId(requester.getId())).thenReturn(Optional.of(credit));

                // when && then
                assertThatThrownBy(() -> creditService.useCreditForExchangeRequest(skillExchange))
                        .isInstanceOf(NotEnoughCreditException.class);
            }
        }
    }

    ///
    @Nested
    @DisplayName("크레딧 수급 - 스킬 교환 취소 및 거절 (requester에게 credit 지급 및 creditHistory 생성)")
    class RefundCreditForExchange {
        private User requester;
        private User receiver;
        private UserSkill receiverSkill;
        private SkillExchange skillExchange;

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("성공")
            public void success() {
                // given
                requester = user;
                receiver = createUser();
                UserSkill receiverSkill = createUserSkill();
                skillExchange = createSkillExchange(requester, receiver, receiverSkill);
                SupplyType supplyType = SupplyType.ADD;
                HistoryType historyType = HistoryType.EXCHANGE_CANCELED;

                // credit 조회 Mock 처리
                when(creditRepository.findByUserId(requester.getId())).thenReturn(Optional.of(credit));

                // when
                creditService.refundCreditForExchange(skillExchange, supplyType, historyType);

                // then
                verify(historyService).createExchangeHistory(
                        eq(requester),
                        eq(receiver),
                        eq(skillExchange),
                        eq(supplyType),
                        eq(skillExchange.getCreditPrice()),
                        eq(skillExchange.getCreditPrice() + credit.getBalance()),
                        eq(historyType));
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("크레딧이 존재하지 않아 CreditNotFoundException")
            public void fail_CreditNotFoundException() {
                // given
                requester = user;
                receiver = createUser();
                UserSkill receiverSkill = createUserSkill();
                skillExchange = createSkillExchange(requester, receiver, receiverSkill);
                SupplyType supplyType = SupplyType.ADD;
                HistoryType historyType = HistoryType.EXCHANGE_CANCELED;

                // credit 조회 Mock 처리
                when(creditRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

                // when && then
                assertThatThrownBy(() -> creditService.refundCreditForExchange(skillExchange, supplyType, historyType))
                        .isInstanceOf(NotFoundCreditException.class);
            }
        }
    }

    private UserSkill createUserSkill(){
        SkillCategory skillCategory = SkillCategory.create(SkillCategoryType.DEVELOPMENT);
        UserSkill userSkill = UserSkill.create(skillCategory, "java", SkillLevel.LOW,
                "description", 30, true);
        ReflectionTestUtils.setField(userSkill, "id", 1L);
        return userSkill;
    }

    private SkillExchange createSkillExchange(User requester, User receiver, UserSkill userSkill){
        SkillExchange skillExchange = SkillExchange.create(requester, receiver, userSkill, LocalDate.now(), LocalTime.now(),
                LocalTime.now().plusMinutes(30), "message");

        ReflectionTestUtils.setField(skillExchange, "id", 1L);
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
}