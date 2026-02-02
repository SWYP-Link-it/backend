package org.swyp.linkit.domain.credit.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.TestRedisConfig;
import org.swyp.linkit.domain.credit.dto.CreditDto;
import org.swyp.linkit.domain.credit.dto.CreditWithUserDetailsDto;
import org.swyp.linkit.domain.credit.entity.Credit;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.entity.SupplyType;
import org.swyp.linkit.domain.credit.repository.CreditHistoryRepository;
import org.swyp.linkit.domain.credit.repository.CreditRepository;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.exchange.repository.SkillExchangeRepository;
import org.swyp.linkit.domain.settlement.entity.Settlement;
import org.swyp.linkit.domain.settlement.repository.SettlementRepository;
import org.swyp.linkit.domain.user.entity.*;
import org.swyp.linkit.domain.user.repository.SkillCategoryRepository;
import org.swyp.linkit.domain.user.repository.UserProfileRepository;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.domain.user.repository.UserSkillRepository;
import org.swyp.linkit.global.error.exception.NotEnoughCreditException;
import org.swyp.linkit.global.error.exception.NotFoundCreditException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Import(TestRedisConfig.class)
@ActiveProfiles("test")
@Transactional
@SpringBootTest(properties = "scheduler.enabled=false")
@DisplayName("CreditService 통합 테스트")
public class CreditServiceImplIntegrationTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private CreditService creditService;
    @Autowired
    private CreditRepository creditRepository;
    @Autowired
    private CreditHistoryRepository historyRepository;
    @Autowired
    private SkillExchangeRepository skillExchangeRepository;
    @Autowired
    private UserSkillRepository userSkillRepository;
    @Autowired
    private SkillCategoryRepository skillCategoryRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("크레딧 생성")
    class CreateCredit {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("신규 유저에게 0개의 크레딧을 가진 크레딧 데이터를 생성한다.")
            public void success() {
                // given
                User savedUser = createSavedUser();
                Long userId = savedUser.getId();

                // when
                CreditDto result = creditService.createCredit(savedUser);
                em.flush();
                em.clear();

                // then
                // DB credit 저장 검증
                assertThat(creditRepository.findByUserId(userId))
                        .isPresent()
                        .get()
                        .satisfies(credit -> {
                            assertThat(credit.getUser().getId()).isEqualTo(userId);
                            assertThat(credit.getId()).isNotNull();
                            assertThat(credit.getBalance()).isEqualTo(0);
                            assertThat(credit.getCreatedAt()).isNotNull();
                        });

                // return Dto 검증
                assertThat(result.getCreditId()).isNotNull();
                assertThat(result.getUserId()).isEqualTo(savedUser.getId());
                assertThat(result.getBalance()).isEqualTo(0);
            }
        }
    }

    @Nested
    @DisplayName("회원가입 리워드 지급")
    class RewardCreditOnSignupSetup {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("회원가입 시 회원가입 리워드 2크레딧을 지급한다.")
            public void success() {
                // given
                int creditAmount = 0;
                User savedUser = createSavedUser();
                Credit savedCredit = createSavedCredit(savedUser, creditAmount);
                Long creditId = savedCredit.getId();
                Long userId = savedUser.getId();

                em.flush();
                em.clear();

                // when
                CreditDto result = creditService.rewardCreditOnSignupSetup(savedUser);

                // then
                // DB credit update 검증
                assertThat(creditRepository.findById(creditId))
                        .isPresent()
                        .get()
                        .satisfies(credit -> {
                            assertThat(credit.getUser().getId()).isEqualTo(userId);
                            assertThat(credit.getId()).isEqualTo(creditId);
                            assertThat(credit.getBalance()).isEqualTo(creditAmount + Credit.SIGNUP_REWARD);
                        });

                // DB creditHistory save 검증
                assertThat(historyRepository.findByUserId(userId))
                        .isPresent()
                        .get()
                        .satisfies(history -> {
                            assertThat(history.getUser().getId()).isEqualTo(userId);
                            assertThat(history.getTargetUser()).isNull();
                            assertThat(history.getSkillExchange()).isNull();
                            assertThat(history.getContentName()).isEqualTo(HistoryType.SIGNUP_REWARD.getContentName());
                            assertThat(history.getSupplyType()).isEqualTo(SupplyType.ADD);
                            assertThat(history.getChangeAmount()).isEqualTo(Credit.SIGNUP_REWARD);
                            assertThat(history.getHistoryType()).isEqualTo(HistoryType.SIGNUP_REWARD);
                        });

                // return Dto 검증
                assertThat(result.getCreditId()).isEqualTo(creditId);
                assertThat(result.getUserId()).isEqualTo(userId);
                assertThat(result.getBalance()).isEqualTo(creditAmount + Credit.SIGNUP_REWARD);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class failCases {
            @Test
            @DisplayName("유저의 크레딧 정보가 존재하지 않아 NotFoundCreditException 발생")
            public void fail_NotFoundCreditException() {
                // given
                User user = createSavedUser();

                // when && then
                assertThatThrownBy(() -> creditService.rewardCreditOnSignupSetup(user))
                        .isInstanceOf(NotFoundCreditException.class);
            }
        }
    }

    @Nested
    @DisplayName("프로필 설정 완료 리워드 지급")
    class RewardCreditOnProfileSetup {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("프로필 설정 완료 시 리워드 1크레딧을 지급한다.")
            public void success() {
                // given
                int creditAmount = 0;
                User savedUser = createSavedUser();
                Credit savedCredit = createSavedCredit(savedUser, creditAmount);
                Long creditId = savedCredit.getId();
                Long userId = savedUser.getId();

                em.flush();
                em.clear();

                // when
                CreditDto result = creditService.rewardCreditOnProfileSetup(savedUser);

                // then
                // DB credit update 검증
                assertThat(creditRepository.findById(creditId))
                        .isPresent()
                        .get()
                        .satisfies(credit -> {
                            assertThat(credit.getUser().getId()).isEqualTo(userId);
                            assertThat(credit.getId()).isEqualTo(creditId);
                            assertThat(credit.getBalance()).isEqualTo(creditAmount + Credit.PROFILE_REWARD);
                        });

                // DB creditHistory save 검증
                assertThat(historyRepository.findByUserId(userId))
                        .isPresent()
                        .get()
                        .satisfies(history -> {
                            assertThat(history.getUser().getId()).isEqualTo(userId);
                            assertThat(history.getTargetUser()).isNull();
                            assertThat(history.getSkillExchange()).isNull();
                            assertThat(history.getContentName()).isEqualTo(HistoryType.PROFILE_REWARD.getContentName());
                            assertThat(history.getSupplyType()).isEqualTo(SupplyType.ADD);
                            assertThat(history.getChangeAmount()).isEqualTo(Credit.PROFILE_REWARD);
                            assertThat(history.getHistoryType()).isEqualTo(HistoryType.PROFILE_REWARD);
                        });

                // return Dto 검증
                assertThat(result.getCreditId()).isEqualTo(creditId);
                assertThat(result.getUserId()).isEqualTo(userId);
                assertThat(result.getBalance()).isEqualTo(creditAmount + Credit.PROFILE_REWARD);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class failCases {
            @Test
            @DisplayName("유저의 크레딧 정보가 존재하지 않아 NotFoundCreditException 발생")
            public void fail_NotFoundCreditException() {
                // given
                User user = createSavedUser();

                // when && then
                assertThatThrownBy(() -> creditService.rewardCreditOnProfileSetup(user))
                        .isInstanceOf(NotFoundCreditException.class);
            }
        }

    }

    @Nested
    @DisplayName("유저의 크레딧 잔액 조회")
    class GetCreditBalance {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("유저의 크레딧 잔액을 조회한다.")
            public void success() {
                // given
                int creditAmount = 5;
                User savedUser = createSavedUser();
                Credit savedCredit = createSavedCredit(savedUser, creditAmount);
                Long userId = savedUser.getId();
                Long creditId = savedCredit.getId();

                em.flush();
                em.clear();
                ;

                // when
                CreditDto result = creditService.getCreditBalance(userId);

                // then
                // return Dto 검증
                assertThat(result.getCreditId()).isEqualTo(creditId);
                assertThat(result.getUserId()).isEqualTo(userId);
                assertThat(result.getBalance()).isEqualTo(creditAmount);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("유저의 크레딧 정보가 존재하지 않아 NotFoundCreditException 발생")
            public void fail_NotFoundCreditException() {
                // given
                User user = createSavedUser();

                em.flush();
                em.clear();

                // when && then
                assertThatThrownBy(() -> creditService.getCreditBalance(user.getId()))
                        .isInstanceOf(NotFoundCreditException.class);
            }
        }
    }

    @Nested
    @DisplayName("크레딧 잔액 및 유저 정보 조회")
    class GetCreditBalanceWithUserDetails {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("유저의 크레딧 잔액 및 유저 정보를 조회합니다.")
            public void success() {
                // given
                int creditAmount = 5;
                User savedUser = createSavedUser();
                Credit savedCredit = createSavedCredit(savedUser, creditAmount);

                Long userId = savedUser.getId();

                em.flush();
                em.clear();

                // when
                CreditWithUserDetailsDto result = creditService.getCreditBalanceWithUserDetails(userId);

                // then
                // return Dto 검증
                assertThat(result.getCreditId()).isEqualTo(savedCredit.getId());
                assertThat(result.getUserId()).isEqualTo(userId);
                assertThat(result.getUserNickname()).isEqualTo(savedUser.getNickname());
                assertThat(result.getUserProfileImageUrl()).isEqualTo(savedUser.getProfileImageUrl());
                assertThat(result.getBalance()).isEqualTo(creditAmount);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("유저의 크레딧 정보가 존재하지 않아 NotFoundCreditException 발생")
            public void fail_NotFoundCreditException() {
                // given
                User user = createSavedUser();

                em.flush();
                em.clear();

                // when
                assertThatThrownBy(() -> creditService.getCreditBalanceWithUserDetails(user.getId()))
                        .isInstanceOf(NotFoundCreditException.class);
            }
        }
    }

    @Nested
    @DisplayName("사용하려는 크레딧 만큼 잔액이 존재하는지 검증")
    class ValidateAvailableBalance {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("잔액이 충분하면 예외가 발생하지 않는다.")
            public void success() {
                // given
                int creditAmount = 5;
                int amount = creditAmount - 1;
                User user = createSavedUser();
                createSavedCredit(user, creditAmount);

                em.flush();
                em.clear();

                // when && then
                assertThatCode(() -> creditService.validateAvailableBalance(user.getId(), amount))
                        .doesNotThrowAnyException();
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {

            @Test
            @DisplayName("유저의 크레딧 정보가 존재하지 않아 NotFoundCreditException 발생")
            public void fail_NotFoundCreditException() {
                // given
                int amount = 1;
                User user = createSavedUser();

                // when
                assertThatThrownBy(() -> creditService.validateAvailableBalance(user.getId(), amount))
                        .isInstanceOf(NotFoundCreditException.class);
            }

            @Test
            @DisplayName("잔액이 요청 금액보다 적으면 NotEnoughCreditException 발생.")
            public void fail_NotEnoughCreditException() {
                int creditAmount = 5;
                int amount = creditAmount + 1;
                User user = createSavedUser();
                createSavedCredit(user, creditAmount);

                em.flush();
                em.clear();

                // when && then
                assertThatThrownBy(() -> creditService.validateAvailableBalance(user.getId(), amount))
                        .isInstanceOf(NotEnoughCreditException.class);
            }
        }
    }

    @Nested
    @DisplayName("스킬 교환 요청 시 요청자의 크레딧을 감소시키고 내역을 남긴다.")
    class UseCreditForExchangeRequest {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {

            @Test
            @DisplayName("요청자의 크레딧을 감소시키고 내역을 생성한다.")
            public void success() {
                // given
                int creditAmount = 5;
                User requester = createSavedUser();
                User receiver = createSavedUser();
                Credit requesterCredit = createSavedCredit(requester, creditAmount);
                SkillCategory skillCategory = createSavedSkillCategory();
                UserSkill receiverSkill = createUserSkill(skillCategory);
                createSavedUserProfile(receiver, receiverSkill);
                SkillExchange skillExchange = createSavedExchange(requester, receiver, receiverSkill);

                int requesterBeforeBalance = requesterCredit.getBalance();

                // when
                creditService.useCreditForExchangeRequest(skillExchange);

                em.flush();
                em.clear();

                // then
                // credit 차감 검증
                Optional<Credit> optionalCredit = creditRepository.findByUserId(requester.getId());
                assertThat(optionalCredit).isPresent();
                Credit foundCredit = optionalCredit.get();

                int expectedChangeAmount = skillExchange.getExchangeDuration() / SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;
                assertThat(foundCredit.getBalance())
                        .isEqualTo(requesterBeforeBalance - expectedChangeAmount);

                // creditHistory 검증
                assertThat(historyRepository.findByUserId(requester.getId()))
                        .isPresent()
                        .get()
                        .satisfies(history -> {
                            assertThat(history.getId()).isNotNull();
                            assertThat(history.getUser().getId()).isEqualTo(requester.getId());
                            assertThat(history.getTargetUser().getId()).isEqualTo(receiver.getId());
                            assertThat(history.getSkillExchange().getId()).isEqualTo(skillExchange.getId());
                            assertThat(history.getContentName()).isEqualTo(receiverSkill.getSkillName());
                            assertThat(history.getSupplyType()).isEqualTo(SupplyType.USE);
                            assertThat(history.getChangeAmount()).isEqualTo(expectedChangeAmount);
                            assertThat(history.getBalanceAfter()).isEqualTo(foundCredit.getBalance());
                            assertThat(history.getHistoryType()).isEqualTo(HistoryType.EXCHANGE_REQUEST);
                        });
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("유저의 크레딧 정보가 존재하지 않아 NotFoundCreditException 발생")
            public void fail_NotFoundCreditException() {
                // given
                User requester = createSavedUser();
                User receiver = createSavedUser();
                SkillCategory skillCategory = createSavedSkillCategory();
                UserSkill receiverSkill = createUserSkill(skillCategory);
                createSavedUserProfile(receiver, receiverSkill);
                SkillExchange skillExchange = createSavedExchange(requester, receiver, receiverSkill);

                // when && then
                assertThatThrownBy(() -> creditService.useCreditForExchangeRequest(skillExchange))
                        .isInstanceOf(NotFoundCreditException.class);
            }

            @Test
            @DisplayName("잔액이 요청 금액보다 적으면 NotEnoughCreditException 발생.")
            public void fail_NotEnoughCreditException() {
                // given
                User requester = createSavedUser();
                User receiver = createSavedUser();
                createSavedCredit(requester, 0);
                SkillCategory skillCategory = createSavedSkillCategory();
                UserSkill receiverSkill = createUserSkill(skillCategory);
                createSavedUserProfile(receiver, receiverSkill);
                SkillExchange skillExchange = createSavedExchange(requester, receiver, receiverSkill);

                // when && then
                assertThatThrownBy(() -> creditService.useCreditForExchangeRequest(skillExchange))
                        .isInstanceOf(NotEnoughCreditException.class);
            }
        }
    }

    @Nested
    @DisplayName("스킬 교환 취소 및 거절 시 요청자의 크레딧을 증가시킨다.")
    class RefundCreditForExchange {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {

            private final int creditAmount = 5;
            private final User requester = createSavedUser();
            private final User receiver = createSavedUser();
            private final Credit requesterCredit = createSavedCredit(requester, creditAmount);
            private final SkillCategory skillCategory = createSavedSkillCategory();
            private final UserSkill receiverSkill = createUserSkill(skillCategory);
            private final UserProfile receiverProfile = createSavedUserProfile(receiver, receiverSkill);
            private final SkillExchange skillExchange = createSavedExchange(requester, receiver, receiverSkill);

            @Test
            @DisplayName("스킬 교환 취소로 인해 요청자의 크레딧을 증가시킨다.")
            public void success_cancel() {
                // given
                int requesterBeforeBalance = requesterCredit.getBalance();
                int creditPrice = receiverSkill.getExchangeDuration() / SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;

                // when
                creditService.refundCreditForExchange(skillExchange, HistoryType.EXCHANGE_CANCELED);

                em.flush();
                em.clear();

                // then
                // credit 지급 검증
                Optional<Credit> optionalCredit = creditRepository.findByUserId(requester.getId());
                assertThat(optionalCredit).isPresent();
                Credit foundCredit = optionalCredit.get();

                assertThat(foundCredit.getBalance()).isEqualTo(requesterBeforeBalance + creditPrice);

                // creditHistory 검증
                int expectedChangeAmount = skillExchange.getExchangeDuration() / SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;
                assertThat(historyRepository.findByUserId(requester.getId()))
                        .isPresent()
                        .get()
                        .satisfies(history -> {
                            assertThat(history.getId()).isNotNull();
                            assertThat(history.getUser().getId()).isEqualTo(requester.getId());
                            assertThat(history.getTargetUser().getId()).isEqualTo(receiver.getId());
                            assertThat(history.getSkillExchange().getId()).isEqualTo(skillExchange.getId());
                            assertThat(history.getContentName()).isEqualTo(receiverSkill.getSkillName());
                            assertThat(history.getSupplyType()).isEqualTo(SupplyType.ADD);
                            assertThat(history.getChangeAmount()).isEqualTo(expectedChangeAmount);
                            assertThat(history.getBalanceAfter()).isEqualTo(requesterBeforeBalance + expectedChangeAmount);
                            assertThat(history.getHistoryType()).isEqualTo(HistoryType.EXCHANGE_CANCELED);
                        });
            }

            @Test
            @DisplayName("스킬 교환 거절로 인해 요청자의 크레딧을 증가시킨다.")
            public void success_reject() {
                // given
                int requesterBeforeBalance = requesterCredit.getBalance();
                int creditPrice = receiverSkill.getExchangeDuration() / SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;

                // when
                creditService.refundCreditForExchange(skillExchange, HistoryType.EXCHANGE_REJECTED);

                em.flush();
                em.clear();

                // then
                // credit 지급 검증
                Optional<Credit> optionalCredit = creditRepository.findByUserId(requester.getId());
                assertThat(optionalCredit).isPresent();
                Credit foundCredit = optionalCredit.get();

                assertThat(foundCredit.getBalance()).isEqualTo(requesterBeforeBalance + creditPrice);

                // creditHistory 검증
                int expectedChangeAmount = skillExchange.getExchangeDuration() / SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;
                assertThat(historyRepository.findByUserId(requester.getId()))
                        .isPresent()
                        .get()
                        .satisfies(history -> {
                            assertThat(history.getId()).isNotNull();
                            assertThat(history.getUser().getId()).isEqualTo(requester.getId());
                            assertThat(history.getTargetUser().getId()).isEqualTo(receiver.getId());
                            assertThat(history.getSkillExchange().getId()).isEqualTo(skillExchange.getId());
                            assertThat(history.getContentName()).isEqualTo(receiverSkill.getSkillName());
                            assertThat(history.getSupplyType()).isEqualTo(SupplyType.ADD);
                            assertThat(history.getChangeAmount()).isEqualTo(expectedChangeAmount);
                            assertThat(history.getBalanceAfter()).isEqualTo(requesterBeforeBalance + expectedChangeAmount);
                            assertThat(history.getHistoryType()).isEqualTo(HistoryType.EXCHANGE_REJECTED);
                        });
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("유저의 크레딧 정보가 존재하지 않아 NotFoundCreditException 발생")
            public void fail_NotFoundCreditException() {
                // given
                User requester = createSavedUser();
                User receiver = createSavedUser();
                SkillCategory skillCategory = createSavedSkillCategory();
                UserSkill receiverSkill = createUserSkill(skillCategory);
                createSavedUserProfile(receiver, receiverSkill);
                SkillExchange skillExchange = createSavedExchange(requester, receiver, receiverSkill);

                // when && then
                assertThatThrownBy(() -> creditService.refundCreditForExchange(skillExchange, HistoryType.EXCHANGE_CANCELED))
                        .isInstanceOf(NotFoundCreditException.class);
            }
        }
    }

    @Nested
    @DisplayName("스킬 교환 정산 시 수신자의 크레딧을 증가시킨다.")
    class SettleCredit {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("스킬 교환 정산 시 수신자의 크레딧을 정산한다.")
            public void success() {
                // given
                int creditAmount = 5;
                User requester = createSavedUser();
                User receiver = createSavedUser();
                Credit receiverCredit = createSavedCredit(receiver, creditAmount);
                SkillCategory skillCategory = createSavedSkillCategory();
                UserSkill receiverSkill = createUserSkill(skillCategory);
                UserProfile receiverProfile = createSavedUserProfile(receiver, receiverSkill);
                SkillExchange skillExchange = createSavedExchange(requester, receiver, receiverSkill);
                Settlement settlement = createSavedSettlement(skillExchange, receiver);

                int receiverBeforeBalance = receiverCredit.getBalance();
                int settlementAmount = settlement.getAmount();

                em.flush();
                em.clear();

                // when
                creditService.settleCredit(settlement);

                // then
                // credit 증가 검증
                Optional<Credit> optionalCredit = creditRepository.findByUserId(receiver.getId());
                assertThat(optionalCredit).isPresent();

                Credit foundCredit = optionalCredit.get();
                assertThat(foundCredit.getBalance()).isEqualTo(receiverBeforeBalance + settlementAmount);

                // creditHistory 검증
                assertThat(historyRepository.findByUserId(receiver.getId()))
                        .isPresent()
                        .get()
                        .satisfies(history -> {
                            assertThat(history.getId()).isNotNull();
                            assertThat(history.getUser().getId()).isEqualTo(receiver.getId());
                            assertThat(history.getTargetUser().getId()).isEqualTo(requester.getId());
                            assertThat(history.getSkillExchange().getId()).isEqualTo(skillExchange.getId());
                            assertThat(history.getContentName()).isEqualTo(receiverSkill.getSkillName());
                            assertThat(history.getSupplyType()).isEqualTo(SupplyType.ADD);
                            assertThat(history.getChangeAmount()).isEqualTo(settlementAmount);
                            assertThat(history.getBalanceAfter()).isEqualTo(receiverBeforeBalance + settlementAmount);
                            assertThat(history.getHistoryType()).isEqualTo(HistoryType.EXCHANGE_SETTLED);
                        });
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("유저의 크레딧 정보가 존재하지 않아 NotFoundCreditException 발생")
            public void fail_NotFoundCreditException() {
                // given
                int creditAmount = 5;
                User requester = createSavedUser();
                User receiver = createSavedUser();
                SkillCategory skillCategory = createSavedSkillCategory();
                UserSkill receiverSkill = createUserSkill(skillCategory);
                UserProfile receiverProfile = createSavedUserProfile(receiver, receiverSkill);
                SkillExchange skillExchange = createSavedExchange(requester, receiver, receiverSkill);
                Settlement settlement = createSavedSettlement(skillExchange, receiver);

                // when && then
                assertThatThrownBy(() -> creditService.settleCredit(settlement))
                        .isInstanceOf(NotFoundCreditException.class);
            }
        }
    }

    private Settlement createSavedSettlement(SkillExchange skillExchange, User receiver){
        return settlementRepository.save(Settlement.create(skillExchange, receiver));
    }

    private UserProfile createSavedUserProfile(User user, UserSkill userSkill) {
        UserProfile userProfile = UserProfile.create(
                user,
                "description"
                , ExchangeType.OFFLINE,
                null,
                null
        );
        userProfile.addUserSkill(userSkill);
        userProfileRepository.save(userProfile);
        userSkillRepository.save(userSkill);
        return userProfile;
    }

    private SkillCategory createSavedSkillCategory() {
        return skillCategoryRepository.findByCategoryType(SkillCategoryType.DEVELOPMENT)
                .orElseGet(() -> skillCategoryRepository.save(SkillCategory.create(SkillCategoryType.DEVELOPMENT)));
    }

    private UserSkill createUserSkill(SkillCategory skillCategory) {
        return UserSkill.create(
                skillCategory,
                "name",
                "title",
                SkillProficiency.MEDIUM,
                "description",
                30
        );
    }

    private SkillExchange createSavedExchange(User requester, User receiver, UserSkill receiverSkill) {
        SkillExchange skillExchange = SkillExchange.create(
                requester,
                receiver,
                receiverSkill,
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now().plusHours(1),
                "defaultMessage"
        );

        return skillExchangeRepository.save(skillExchange);
    }

    private Credit createSavedCredit(User user, int amount) {
        Credit credit = Credit.create(user, amount);
        return creditRepository.save(credit);
    }

    private User createSavedUser() {
        String uuid = UUID.randomUUID().toString();
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_1" + uuid,
                uuid + "@test.com",
                "tester" + uuid,
                "https://image",
                "testerNickname" + uuid);

        return userRepository.save(user);
    }
}
