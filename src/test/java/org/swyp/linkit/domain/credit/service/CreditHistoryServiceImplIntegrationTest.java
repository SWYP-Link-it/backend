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
import org.swyp.linkit.domain.credit.dto.RewardHistoryDto;
import org.swyp.linkit.domain.credit.entity.Credit;
import org.swyp.linkit.domain.credit.entity.CreditHistory;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.entity.SupplyType;
import org.swyp.linkit.domain.credit.repository.CreditHistoryRepository;
import org.swyp.linkit.domain.credit.repository.CreditRepository;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.exchange.repository.SkillExchangeRepository;
import org.swyp.linkit.domain.user.entity.*;
import org.swyp.linkit.domain.user.repository.SkillCategoryRepository;
import org.swyp.linkit.domain.user.repository.UserProfileRepository;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.domain.user.repository.UserSkillRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestRedisConfig.class)
@ActiveProfiles("test")
@Transactional
@SpringBootTest(properties = "scheduler.enabled=false")
@DisplayName("CreditHistoryService 통합 테스트")
public class CreditHistoryServiceImplIntegrationTest {

    @Autowired
    private EntityManager em;
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
    private UserRepository userRepository;
    @Autowired
    private CreditHistoryService creditHistoryService;

    @Nested
    @DisplayName("리워드 크레딧 내역을 생성한다.")
    class CreateRewardHistory{

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("성공")
            public void success() {
                // given
                User savedUser = createSavedUser();
                Credit savedCredit = createSavedCredit(savedUser, 0);
                int beforeBalance = savedCredit.getBalance();
                int amount = Credit.SIGNUP_REWARD;
                int balanceAfter = amount + beforeBalance;
                HistoryType historyType = HistoryType.SIGNUP_REWARD;

                // when
                RewardHistoryDto result = creditHistoryService
                        .createRewardHistory(savedUser, amount, balanceAfter, historyType);
                em.flush();
                em.clear();

                // then
                // creditHistory 생성 검증
                Optional<CreditHistory> optionalCreditHistory = historyRepository.findTopByUserIdOrderByIdDesc(savedUser.getId());
                assertThat(optionalCreditHistory).isPresent();

                CreditHistory foundHistory = optionalCreditHistory.get();

                assertThat(foundHistory.getUser().getId()).isEqualTo(savedUser.getId());
                assertThat(foundHistory.getTargetUser()).isNull();
                assertThat(foundHistory.getSkillExchange()).isNull();
                assertThat(foundHistory.getContentName()).isEqualTo(historyType.getContentName());
                assertThat(foundHistory.getSupplyType()).isEqualTo(SupplyType.ADD);
                assertThat(foundHistory.getChangeAmount()).isEqualTo(amount);
                assertThat(foundHistory.getHistoryType()).isEqualTo(historyType);

                // return Dto 검증
                assertThat(result.getHistoryId()).isEqualTo(foundHistory.getId());
                assertThat(result.getHistoryType()).isEqualTo(historyType);
                assertThat(result.getUserId()).isEqualTo(foundHistory.getUser().getId());
                assertThat(result.getChangeAmount()).isEqualTo(Credit.SIGNUP_REWARD);
                assertThat(result.getContentName()).isEqualTo(historyType.getContentName());
                assertThat(result.getBalanceAfter()).isEqualTo(balanceAfter);
                assertThat(result.getSupplyType()).isEqualTo(SupplyType.ADD);
            }
        }
    }

    @Nested
    @DisplayName("스킬 교환 크레딧 내역을 생성한다.")
    class CreateExchangeHistory{

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("성공")
            public void success() {
                // given
                User requester = createSavedUser();
                User receiver = createSavedUser();
                Credit requesterCredit = createSavedCredit(requester, 5);
                SkillCategory skillCategory = createSavedSkillCategory();
                UserSkill receiverSkill = createUserSkill(skillCategory);
                UserProfile receiverProfile = createSavedUserProfile(receiver, receiverSkill);
                SkillExchange skillExchange = createSavedExchange(requester, receiver, receiverSkill);

                int amount = 2;
                int balanceAfter = requesterCredit.getBalance() - amount;
                HistoryType historyType = HistoryType.EXCHANGE_REQUEST;

                // when
                CreditHistory result = creditHistoryService.createExchangeHistory(requester, receiver,
                        skillExchange, SupplyType.USE, amount, balanceAfter, historyType);
                em.flush();
                em.clear();

                // then
                assertThat(result.getId()).isNotNull();
                assertThat(result.getUser().getId()).isEqualTo(requester.getId());
                assertThat(result.getTargetUser().getId()).isEqualTo(receiver.getId());
                assertThat(result.getSkillExchange().getId()).isEqualTo(skillExchange.getId());
                assertThat(result.getSupplyType()).isEqualTo(SupplyType.USE);
                assertThat(result.getChangeAmount()).isEqualTo(amount);
                assertThat(result.getBalanceAfter()).isEqualTo(balanceAfter);
                assertThat(result.getHistoryType()).isEqualTo(historyType);
            }
        }
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
