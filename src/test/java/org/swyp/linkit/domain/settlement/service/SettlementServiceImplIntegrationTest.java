//package org.swyp.linkit.domain.settlement.service;
//
//import jakarta.persistence.EntityManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//import org.swyp.linkit.TestRedisConfig;
//import org.swyp.linkit.domain.chat.repository.ChatRoomRepository;
//import org.swyp.linkit.domain.credit.entity.Credit;
//import org.swyp.linkit.domain.credit.repository.CreditHistoryRepository;
//import org.swyp.linkit.domain.credit.repository.CreditRepository;
//import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
//import org.swyp.linkit.domain.exchange.entity.SkillExchange;
//import org.swyp.linkit.domain.exchange.repository.SkillExchangeRepository;
//import org.swyp.linkit.domain.exchange.service.SkillExchangeService;
//import org.swyp.linkit.domain.settlement.entity.Settlement;
//import org.swyp.linkit.domain.settlement.entity.SettlementStatus;
//import org.swyp.linkit.domain.settlement.repository.SettlementRepository;
//import org.swyp.linkit.domain.user.entity.*;
//import org.swyp.linkit.domain.user.repository.SkillCategoryRepository;
//import org.swyp.linkit.domain.user.repository.UserProfileRepository;
//import org.swyp.linkit.domain.user.repository.UserRepository;
//import org.swyp.linkit.domain.user.repository.UserSkillRepository;
//import org.swyp.linkit.global.error.exception.InvalidSettlementStatusException;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//@Import(TestRedisConfig.class)
//@ActiveProfiles("test")
//@SpringBootTest(properties = "scheduler.enabled=false")
//@DisplayName("SettlementService 통합 테스트")
//public class SettlementServiceImplIntegrationTest {
//
//    @Autowired
//    private EntityManager em;
//    @Autowired
//    private SettlementService settlementService;
//    @Autowired
//    private SettlementRepository settlementRepository;
//    @Autowired
//    private SkillExchangeService skillExchangeService;
//    @Autowired
//    private UserProfileRepository userProfileRepository;
//    @Autowired
//    private UserSkillRepository userSkillRepository;
//    @Autowired
//    private SkillCategoryRepository skillCategoryRepository;
//    @Autowired
//    private CreditHistoryRepository creditHistoryRepository; // 추가된 부분
//    @Autowired
//    private SkillExchangeRepository skillExchangeRepository;
//    @Autowired
//    private CreditRepository creditRepository;
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private ChatRoomRepository chatRoomRepository;
//
//    private SkillCategory skillCategory;
//    @BeforeEach
//    void setupSkillCategory(){
//        skillCategory = createSavedSkillCategory();
//    }
//
//    @Transactional
//    @Nested
//    @DisplayName("정산 데이터 생성")
//    class CreateSettlement {
//
//        @Nested
//        @DisplayName("성공 케이스")
//        class SuccessCases {
//            @Test
//            @DisplayName("스킬 거래 요청을 수락할 시 Settlement를 생성한다.")
//            public void success() {
//                // given
//                User requester = createSavedUser();
//                User receiver = createSavedUser();
//
//                int exchangeDuration = 60;
//                UserSkill receiverSkill = createUserSkill(skillCategory, exchangeDuration);
//                createSavedUserProfile(receiver, receiverSkill);
//
//                // 스킬 거래 생성
//                SkillExchange skillExchange = createSavedExchange(requester, receiver, receiverSkill, LocalDate.now(),
//                        LocalTime.now().minusHours(3), LocalTime.now().minusHours(2));
//
//                Long skillExchangeId = skillExchange.getId();
//
//                // when
//                settlementService.createSettlement(skillExchange);
//                em.flush();
//                em.clear();
//
//                // then
//                // DB 검증
//                assertThat(settlementRepository.findBySkillExchangeId(skillExchangeId))
//                        .isPresent()
//                        .get()
//                        .satisfies(s -> {
//                            assertThat(s.getId()).isNotNull();
//                            assertThat(s.getSkillExchange().getId()).isEqualTo(skillExchangeId);
//                            assertThat(s.getReceiver().getId()).isEqualTo(receiver.getId());
//                            assertThat(s.getAmount()).isEqualTo(skillExchange.getCreditPrice());
//                            assertThat(s.getStatus()).isEqualTo(SettlementStatus.PENDING);
//                            assertThat(s.getSettledAt()).isNull();
//                        });
//            }
//        }
//    }
//
//    @Transactional
//    @Nested
//    @DisplayName("정산 상태를 PENDING 에서 CANCELED로 변경.")
//    class CancelSettlement {
//
//        @Nested
//        @DisplayName("성공 케이스")
//        class SuccessCases {
//            @Test
//            @DisplayName("스킬 거래 ID를 받아 정산 상태를 PENDING 에서 CANCELD로 변경한다.")
//            public void success() {
//                // given
//                User requester = createSavedUser();
//                User receiver = createSavedUser();
//
//                int exchangeDuration = 60;
//                UserSkill receiverSkill = createUserSkill(skillCategory, exchangeDuration);
//                createSavedUserProfile(receiver, receiverSkill);
//
//                // 스킬 거래 생성
//                SkillExchange skillExchange = createSavedExchange(requester, receiver, receiverSkill, LocalDate.now(),
//                        LocalTime.now().minusHours(3), LocalTime.now().minusHours(2));
//                Long skillExchangeId = skillExchange.getId();
//
//                // 정산 생성
//                createSavedSettlement(skillExchange, receiver);
//
//                // when
//                settlementService.cancelSettlement(skillExchangeId);
//
//                em.flush();
//                em.clear();
//
//                // then
//                // DB 검증
//                assertThat(settlementRepository.findBySkillExchangeId(skillExchangeId))
//                        .isPresent()
//                        .get()
//                        .satisfies(s -> {
//                            // CANCELED 검증
//                            assertThat(s.getStatus()).isEqualTo(SettlementStatus.CANCELED);
//                        });
//            }
//        }
//
//        @Nested
//        @DisplayName("실패 케이스")
//        class FailCases {
//            @Test
//            @DisplayName("PENDING 이 아닌 정산의 상태를 CANCELED로 변경 시도로 인한 InvalidSettlementStatusException.")
//            public void fail_InvalidSettlementStatusException() {
//                // given
//                User requester = createSavedUser();
//                User receiver = createSavedUser();
//
//                int exchangeDuration = 60;
//                UserSkill receiverSkill = createUserSkill(skillCategory, exchangeDuration);
//                createSavedUserProfile(receiver, receiverSkill);
//
//                // 스킬 거래 생성
//                SkillExchange skillExchange = createSavedExchange(requester, receiver, receiverSkill, LocalDate.now(),
//                        LocalTime.now().minusHours(3), LocalTime.now().minusHours(2));
//                Long skillExchangeId = skillExchange.getId();
//
//                // 정산 생성
//                Settlement settlement = createSavedSettlement(skillExchange, receiver);
//                // complete로 변경하여 예외 발생 기대
//                settlement.complete();
//
//                em.flush();
//                em.clear();
//
//                // when && then
//                assertThatThrownBy(() -> settlementService.cancelSettlement(skillExchangeId))
//                        .isInstanceOf(InvalidSettlementStatusException.class);
//            }
//        }
//    }
//
//    @Nested
//    @DisplayName("스킬 거래가 완료된 거래들을 정산 처리.")
//    class ProcessAutoSettlement {
//
//        @Nested
//        @DisplayName("성공 케이스")
//        class SuccessCases {
//
//            @Test
//            @DisplayName("스킬 거래가 완료된 거래들을 조회하여 크레딧을 지급하고, 가르친 횟수를 증가시킨다.")
//            public void success() {
//                // given
//                User requester = createSavedUser();
//                User receiver = createSavedUser();
//                Credit receiverCredit = createSavedCredit(receiver, 0);
//                int receiverBeforeBalance = receiverCredit.getBalance();
//
//                int exchangeDuration = 60;
//                int creditPrice = exchangeDuration / SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;
//                UserSkill receiverSkill = createUserSkill(skillCategory, exchangeDuration);
//                UserProfile receiverProfile = createSavedUserProfile(receiver, receiverSkill);
//                int receiverBeforeTaughtTimes = receiverProfile.getTimesTaught();
//
//                LocalDate today = LocalDate.now();
//                LocalTime now = LocalTime.now();
//
//                // 현재 시간 기준 정산 대상
//                // 1시간 전 시작, 지금 종료
//                SkillExchange targetExchange = createSavedExchange(requester, receiver, receiverSkill, today,
//                        now.minusHours(60), now);
//                targetExchange.updateExchangeStatus(ExchangeStatus.ACCEPTED);
//                createSavedSettlement(targetExchange, receiver);
//                skillExchangeRepository.saveAndFlush(targetExchange);
//
//                // 정산 대상 아님
//                // 다음 날 스킬 거래
//                SkillExchange noneTargetExchange = createSavedExchange(requester, receiver, receiverSkill, today.plusDays(1),
//                        now.minusHours(60), now);
//                noneTargetExchange.updateExchangeStatus(ExchangeStatus.ACCEPTED);
//                createSavedSettlement(noneTargetExchange, receiver);
//                skillExchangeRepository.saveAndFlush(noneTargetExchange);
//
//                // when
//                System.out.println("=== 로직 시작 ==");
//                int successCount = settlementService.processAutoSettlement();
//
//                // then
//                // 정산 처리된 데이터는 1건
//                assertThat(successCount).isEqualTo(1);
//
//                // 트랜잭션 종료로 인해 lazyLoading 사용 불가능
//                // DB 검증
//                // 1. 대상 건 검증
//                // settlement 상태 검증
//                assertThat(settlementRepository.findBySkillExchangeId(targetExchange.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(s -> {
//                              assertThat(s.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
//                        });
//
//                // skillExchange 상태 검증
//                assertThat(skillExchangeRepository.findById(targetExchange.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(se -> {
//                            // settlement 상태 검증
//                            assertThat(se.getExchangeStatus()).isEqualTo(ExchangeStatus.SETTLED);
//                        });
//
//                // 가르친 횟수 검증
//                assertThat(userProfileRepository.findByUserId(receiver.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(rp -> {
//                            assertThat(rp.getTimesTaught()).isEqualTo(receiverBeforeTaughtTimes + 1);
//                        });
//
//                // credit 수급 검증
//                assertThat(creditRepository.findByUserId(receiver.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(c -> {
//                            assertThat(c.getBalance()).isEqualTo(receiverBeforeBalance + creditPrice);
//                        });
//
//                // 2. 대상 아닌 건 검증
//                // settlement 상태 검증
//                assertThat(settlementRepository.findBySkillExchangeId(noneTargetExchange.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(s -> {
//                            assertThat(s.getStatus()).isEqualTo(SettlementStatus.PENDING);
//                        });
//
//                // skillExchange 상태 검증
//                assertThat(skillExchangeRepository.findById(noneTargetExchange.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(se -> {
//                            // settlement 상태 검증
//                            assertThat(se.getExchangeStatus()).isEqualTo(ExchangeStatus.ACCEPTED);
//                        });
//
//                cleanUp();
//            }
//        }
//
//        @Nested
//        @DisplayName("실패 케이스")
//        class FailCases {
//            @Test
//            @DisplayName("여러 건 중 하나가 실패하더라도 REQUIRES_NEW에 의해 다른 건들은 정상 처리되어야 한다.")
//            public void success_IndependentTransaction() {
//                User requester = createSavedUser();
//                User receiver = createSavedUser();
//                Credit receiverCredit = createSavedCredit(receiver, 0);
//                int receiverBeforeBalance = receiverCredit.getBalance();
//
//                int exchangeDuration = 60;
//                int creditPrice = exchangeDuration / SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;
//                UserSkill receiverSkill = createUserSkill(skillCategory, exchangeDuration);
//                UserProfile receiverProfile = createSavedUserProfile(receiver, receiverSkill);
//                int receiverBeforeTaughtTimes = receiverProfile.getTimesTaught();
//
//                LocalDate today = LocalDate.now();
//                LocalTime now = LocalTime.now();
//
//                // 현재 시간 기준 정산 대상
//                // 1시간 전 시작, 지금 종료
//                SkillExchange targetExchange = createSavedExchange(requester, receiver, receiverSkill, today,
//                        now.minusHours(60), now);
//                targetExchange.updateExchangeStatus(ExchangeStatus.ACCEPTED);
//                createSavedSettlement(targetExchange, receiver);
//                skillExchangeRepository.saveAndFlush(targetExchange);
//
//                // 스킬 거래 상태가 PENDING 이라 예외 발생
//                SkillExchange noneTargetExchange = createSavedExchange(requester, receiver, receiverSkill, today,
//                        now.minusHours(60), now);
//                createSavedSettlement(noneTargetExchange, receiver);
//                skillExchangeRepository.saveAndFlush(noneTargetExchange);
//
//                // when
//                int successCount = settlementService.processAutoSettlement();
//
//                // then
//                assertThat(successCount).isEqualTo(1);
//
//                // 1. 실패 건 DB 검증
//                // settlement 상태 검증
//                assertThat(settlementRepository.findBySkillExchangeId(noneTargetExchange.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(s -> {
//                            assertThat(s.getStatus()).isEqualTo(SettlementStatus.PENDING);
//                        });
//
//                // skillExchange 상태 검증
//                assertThat(skillExchangeRepository.findById(noneTargetExchange.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(se -> {
//                            // settlement 상태 검증
//                            assertThat(se.getExchangeStatus()).isEqualTo(ExchangeStatus.PENDING);
//                        });
//
//                // 가르친 횟수 검증
//                assertThat(userProfileRepository.findByUserId(receiver.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(rp -> {
//                            assertThat(rp.getTimesTaught()).isEqualTo(receiverBeforeTaughtTimes + 1);
//                        });
//
//                // credit 수급 검증
//                assertThat(creditRepository.findByUserId(receiver.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(c -> {
//                            assertThat(c.getBalance()).isEqualTo(receiverBeforeBalance + creditPrice);
//                        });
//
//                // 2. 성공 건 DB 검증
//                assertThat(settlementRepository.findBySkillExchangeId(targetExchange.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(s -> {
//                            assertThat(s.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
//                        });
//
//                // skillExchange 상태 검증
//                assertThat(skillExchangeRepository.findById(targetExchange.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(se -> {
//                            // settlement 상태 검증
//                            assertThat(se.getExchangeStatus()).isEqualTo(ExchangeStatus.SETTLED);
//                        });
//
//                cleanUp();
//            }
//
//        }
//    }
//
//    private void cleanUp() {
//        settlementRepository.deleteAllInBatch();
//        creditHistoryRepository.deleteAllInBatch();
//        chatRoomRepository.deleteAllInBatch();
//        skillExchangeRepository.deleteAllInBatch();
//        userSkillRepository.deleteAllInBatch();
//        userProfileRepository.deleteAllInBatch();
//        creditRepository.deleteAllInBatch();
//        userRepository.deleteAllInBatch();
//        skillCategoryRepository.deleteAllInBatch();
//    }
//
//
//    private Settlement createSavedSettlement(SkillExchange skillExchange, User receiver) {
//        return settlementRepository.save(Settlement.create(skillExchange, receiver));
//    }
//
//    private UserProfile createSavedUserProfile(User user, UserSkill userSkill) {
//        UserProfile userProfile = UserProfile.create(
//                user,
//                "description"
//                , ExchangeType.OFFLINE,
//                null,
//                null
//        );
//        userProfile.addUserSkill(userSkill);
//        userProfileRepository.save(userProfile);
//        userSkillRepository.save(userSkill);
//        return userProfile;
//    }
//
//    private SkillCategory createSavedSkillCategory() {
//        return skillCategoryRepository.findByCategoryType(SkillCategoryType.DEVELOPMENT)
//                .orElseGet(() -> skillCategoryRepository.save(SkillCategory.create(SkillCategoryType.DEVELOPMENT)));
//    }
//
//    private UserSkill createUserSkill(SkillCategory skillCategory, int exchangeDuration) {
//        return UserSkill.create(
//                skillCategory,
//                "name",
//                "title",
//                SkillProficiency.MEDIUM,
//                "description",
//                exchangeDuration
//        );
//    }
//
//    private SkillExchange createSavedExchange(User requester, User receiver, UserSkill receiverSkill,
//                                              LocalDate scheduleDate, LocalTime start, LocalTime end) {
//        SkillExchange skillExchange = SkillExchange.create(
//                requester,
//                receiver,
//                receiverSkill,
//                scheduleDate,
//                start,
//                end,
//                "defaultMessage"
//        );
//
//        return skillExchangeRepository.save(skillExchange);
//    }
//
//    private Credit createSavedCredit(User user, int amount) {
//        Credit credit = Credit.create(user, amount);
//        return creditRepository.save(credit);
//    }
//
//    private User createSavedUser() {
//        String uuid = UUID.randomUUID().toString();
//        User user = User.create(
//                OAuthProvider.KAKAO,
//                "kakao_1" + uuid,
//                uuid + "@test.com",
//                "tester" + uuid,
//                "https://image",
//                "testerNickname" + uuid);
//
//        return userRepository.save(user);
//    }
//}
