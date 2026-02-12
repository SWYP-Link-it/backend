//package org.swyp.linkit.domain.settlement.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.swyp.linkit.domain.exchange.entity.SkillExchange;
//import org.swyp.linkit.domain.settlement.entity.Settlement;
//import org.swyp.linkit.domain.settlement.entity.SettlementStatus;
//import org.swyp.linkit.domain.settlement.repository.SettlementRepository;
//import org.swyp.linkit.domain.user.entity.*;
//import org.swyp.linkit.global.error.exception.InvalidSettlementStatusException;
//import org.swyp.linkit.global.error.exception.SettlementNotFoundException;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("SettlementService 단위 테스트")
//class SettlementServiceImplTest {
//
//    @Mock
//    SettlementRepository settlementRepository;
//
//    @Mock
//    SettlementProcessor settlementProcessor;
//
//    @InjectMocks
//    SettlementServiceImpl settlementService;
//
//    private Long userId = 1L;
//    private Long exchangeId = 1L;
//    private Long settlementId = 1L;
//
//    @Test
//    @DisplayName("Settlement 생성")
//    public void createSettlement() {
//        // given
//        User requester = createUser();
//        User receiver = createUser();
//        UserSkill receiverSkill = createUserSkill();
//        UserProfile receiverProfile = createProfile(receiver);
//        receiverProfile.addUserSkill(receiverSkill);
//
//        SkillExchange exchange = createExchange(requester, receiver, receiverSkill);
//
//        Settlement settlement = createSettlement(exchange, receiver);
//
//        // when
//        settlementService.createSettlement(exchange);
//
//        // then
//        verify(settlementRepository, atMost(1)).save(settlement);
//    }
//
//
//    @Nested
//    @DisplayName("거래 수락 후 취소 시 정산 상태 변경(PENDING -> CANCELED)")
//    class CancelSettlement{
//        private User requester;
//        private User receiver;
//        private UserSkill receiverSkill;
//        private UserProfile receiverProfile;
//
//        @BeforeEach
//        void setup(){
//            requester = createUser();
//            receiver = createUser();
//            receiverSkill = createUserSkill();
//            receiverProfile = createProfile(receiver);
//            receiverProfile.addUserSkill(receiverSkill);
//        }
//
//        @Nested
//        @DisplayName("성공 케이스")
//        class SuccessCases {
//            @Test
//            @DisplayName("성공")
//            public void success() {
//                // given
//                SkillExchange exchange = createExchange(requester, receiver, receiverSkill);
//                Long exchangeId = exchange.getId();
//                Settlement settlement = createSettlement(exchange, receiver);
//
//                // settlement 조회 Mock
//                when(settlementRepository.findBySkillExchangeId(exchangeId)).thenReturn(Optional.of(settlement));
//
//                // when
//                settlementService.cancelSettlement(exchangeId);
//
//                // then
//                // settlement PENDING -> CANCELED
//                assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.CANCELED);
//            }
//        }
//
//        @Nested
//        @DisplayName("실패 케이스")
//        class FailCases {
//            @Test
//            @DisplayName("settlement 가 존재 하지 않아 SettlementNotFoundException")
//            public void fail_SettlementNotFoundException() {
//                // given
//                SkillExchange exchange = createExchange(requester, receiver, receiverSkill);
//                Long exchangeId = exchange.getId();
//
//                // settlement 조회 Mock
//                when(settlementRepository.findBySkillExchangeId(exchangeId)).thenReturn(Optional.empty());
//
//                // when && then
//                assertThatThrownBy(() -> settlementService.cancelSettlement(exchangeId))
//                        .isInstanceOf(SettlementNotFoundException.class);
//            }
//
//            @Test
//            @DisplayName("settlementStatus 가 PENDING이 아니기에 InvalidSettlementStatusException")
//            public void fail_InvalidSettlementStatusException() {
//                // given
//                SkillExchange exchange = createExchange(requester, receiver, receiverSkill);
//                Long exchangeId = exchange.getId();
//                Settlement settlement = createSettlement(exchange, receiver);
//                // CANCEL 처리 -> 예외
//                settlement.cancel();
//
//                // settlement 조회 Mock
//                when(settlementRepository.findBySkillExchangeId(exchangeId)).thenReturn(Optional.of(settlement));
//
//                // when && then
//                assertThatThrownBy(() -> settlementService.cancelSettlement(exchangeId))
//                        .isInstanceOf(InvalidSettlementStatusException.class);
//            }
//        }
//    }
//
//    private SkillCategory creatSkillCategory() {
//        return SkillCategory.create(SkillCategoryType.DEVELOPMENT);
//    }
//
//    private UserProfile createProfile(User user) {
//        return UserProfile.create(
//                user,
//                "description",
//                ExchangeType.ONLINE,
//                null,
//                null);
//    }
//
//    private SkillExchange createExchange(User requester, User receiverUser, UserSkill receiverSkill) {
//        SkillExchange skillExchange = SkillExchange.create(
//                requester, receiverUser, receiverSkill, LocalDate.now(),
//                LocalTime.now().minusHours(1), LocalTime.now().minusHours(2), "message");
//        ReflectionTestUtils.setField(skillExchange, "id", exchangeId++);
//        return skillExchange;
//    }
//
//    private User createUser() {
//        User user = User.create(
//                OAuthProvider.KAKAO,
//                "kakao" + userId,
//                "email@example.com" + userId,
//                "name" + userId,
//                "https://image",
//                "nickname" + userId);
//        ReflectionTestUtils.setField(user, "id", userId++);
//        return user;
//    }
//
//    private Settlement createSettlement(SkillExchange exchange, User receiver) {
//        Settlement settlement = Settlement.create(exchange, receiver);
//        ReflectionTestUtils.setField(settlement, "id", settlementId++);
//        return settlement;
//    }
//
//    private UserSkill createUserSkill() {
//        SkillCategory skillCategory = SkillCategory.create(SkillCategoryType.DEVELOPMENT);
//        UserSkill userSkill = UserSkill.create(skillCategory, "java", "title",
//                SkillProficiency.LOW, "description", 30);
//        ReflectionTestUtils.setField(userSkill, "id", 1L);
//        return userSkill;
//    }
//}