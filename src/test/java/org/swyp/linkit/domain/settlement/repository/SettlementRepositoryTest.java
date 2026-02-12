//package org.swyp.linkit.domain.settlement.repository;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.context.annotation.Import;
//import org.swyp.linkit.domain.exchange.entity.SkillExchange;
//import org.swyp.linkit.domain.settlement.entity.Settlement;
//import org.swyp.linkit.domain.settlement.entity.SettlementStatus;
//import org.swyp.linkit.domain.user.entity.*;
//import org.swyp.linkit.global.config.JpaAuditingConfig;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//@DataJpaTest
//@Import(JpaAuditingConfig.class)
//@DisplayName("SettlementRepository 단위 테스트")
//class SettlementRepositoryTest {
//
//    @Autowired
//    TestEntityManager em;
//
//    @Autowired
//    SettlementRepository settlementRepository;
//
//    private User requester;
//    private User receiver;
//    private UserSkill receiverSkill;
//    private UserProfile receiverProfile;
//    private SkillExchange pastExchange;
//    private Settlement targetSettle;
//    private LocalDate today = LocalDate.now();
//    private LocalTime now = LocalTime.now();
//
//    @BeforeEach
//    void setup(){
//        // 정산 대상인 pastExchange 1건, 정산 대상 아닌 exchange 1건
//        testSetup();
//    }
//
//    @Test
//    @DisplayName("skillExchangeId로 settlement 조회")
//    public void findBySkillExchangeId() {
//        // given
//        Long exchangeId = pastExchange.getId();
//
//        // when
//        System.out.println("== 쿼리 시작 ==");
//        Optional<Settlement> optionalSettlement = settlementRepository.findBySkillExchangeId(exchangeId);
//
//        // then
//        assertThat(optionalSettlement).isPresent();
//        Settlement sut = optionalSettlement.get();
//
//        assertThat(sut.getId()).isEqualTo(targetSettle.getId());
//        assertThat(sut.getSkillExchange().getId()).isEqualTo(exchangeId);
//    }
//
//    @Test
//    @DisplayName("정산 처리 대상인 settlement 조회")
//    public void findSettleTargets() {
//        // given
//        SettlementStatus pendingStatus = SettlementStatus.PENDING;
//
//        // when
//        System.out.println("== 쿼리 시작 ==");
//        List<Long> sut = settlementRepository.findSettleTargets(pendingStatus, today, now);
//
//        // then
//        assertThat(sut.size()).isEqualTo(1);
//    }
//
//    private void testSetup(){
//        // requester
//        // receiver
//        requester = createUser();
//        receiver = createUser();
//        em.persist(requester);
//        em.persist(receiver);
//
//        // receiver Profile, skill
//        SkillCategory skillCategory = createSkillCategory(SkillCategoryType.DEVELOPMENT);
//        em.persist(skillCategory);
//
//        receiverSkill = createUserSkill(skillCategory);
//        receiverProfile = createProfile(receiver, receiverSkill);
//        em.persist(receiverProfile);
//        em.persist(receiverSkill);
//
//        // skillExchange, settlement
//        // 1. 정산 대상 -> 오늘 날짜이며 종료 시간이 1시간 전인 거래
//        pastExchange = createExchange(requester, receiver, receiverSkill,
//                today.minusDays(1), now, now.plusMinutes(30));
//        em.persist(pastExchange);
//        targetSettle = Settlement.create(pastExchange, receiver);
//        em.persist(targetSettle);
//
//        // 2. 정산 대상 아님 -> 오늘 날짜이지만 종료 시간이 1시간 후인 거래
//        SkillExchange futureExchange = createExchange(requester, receiver, receiverSkill,
//                today.plusDays(1), now, now.plusMinutes(30));
//        em.persist(futureExchange);
//        Settlement nonTargetSettle = Settlement.create(futureExchange, receiver);
//        em.persist(nonTargetSettle);
//
//        em.flush();
//        em.clear();
//    }
//
//    private SkillExchange createExchange(User requester, User receiver, UserSkill receiverSkill,
//                                         LocalDate scheduledDate, LocalTime start, LocalTime end) {
//        return SkillExchange.create(
//                requester,
//                receiver,
//                receiverSkill,
//                scheduledDate,
//                start,
//                end,
//                "defaultMessage");
//    }
//
//
//    private User createUser() {
//        String uuid = UUID.randomUUID().toString();
//        return User.create(
//                OAuthProvider.KAKAO,
//                "kakao" + uuid,
//                uuid + "email@example.com",
//                "name" + uuid,
//                "https://image",
//                "nickname" + uuid);
//    }
//
//    private SkillCategory createSkillCategory(SkillCategoryType type) {
//        return SkillCategory.create(type);
//    }
//
//    private UserSkill createUserSkill(SkillCategory type) {
//        return UserSkill.create(
//                type,
//                "skillName",
//                "skillTitle",
//                SkillProficiency.HIGH,
//                "description",
//                30);
//    }
//
//    private UserProfile createProfile(User user, UserSkill userSkill) {
//        UserProfile userProfile = UserProfile.create(
//                user,
//                "description",
//                ExchangeType.ONLINE,
//                null,
//                null);
//        userProfile.addUserSkill(userSkill);
//        return userProfile;
//    }
//}