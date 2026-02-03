package org.swyp.linkit.domain.credit.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.swyp.linkit.domain.credit.entity.CreditHistory;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.entity.SupplyType;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.user.entity.*;
import org.swyp.linkit.global.config.JpaAuditingConfig;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("CreditHistoryRepositoryTest 단위 테스트")
class CreditHistoryRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    CreditHistoryRepository historyRepository;

    private User user;
    private User targetUser;
    private int amount = 3;
    private int balanceAfter = 3;

    @BeforeEach
    void setup() {
        user = createUser();
        em.persist(user);
        targetUser = createUser();
        em.persist(targetUser);

        UserProfile targetProfile = createProfile(targetUser);
        UserSkill targetSkill = createUserSkill();
        targetProfile.addUserSkill(targetSkill);
        em.persist(targetProfile);
        em.persist(targetSkill);

        SkillExchange skillExchange = createSkillExchange(user, targetUser, targetSkill);
        em.persist(skillExchange);

        for (int i = 1; i <= 20; i++) {
            if (i % 2 == 0) {
                CreditHistory exchangeHistory = createExchangeHistory(skillExchange);
                em.persist(exchangeHistory);
            } else {
                CreditHistory rewardHistory = createRewardHistory(amount, balanceAfter);
                em.persist(rewardHistory);
            }
        }
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("크레딧 사용 내역 페이징 조회")
    public void findAllByUserIdAndSupplyType() {
        // given
        Long userId = user.getId();
        SupplyType supplyType = SupplyType.USE;
        int size = 3;
        Pageable pageable = PageRequest.of(0, size);

        // when && then
        Slice<CreditHistory> firstSlice = historyRepository.
                findAllByUserIdAndSupplyType(userId, null, null, pageable);

        assertThat(firstSlice.getContent()).hasSize(3);
        assertThat(firstSlice.hasNext()).isTrue();
        Long lastId = firstSlice.getContent().get(2).getId();

        Slice<CreditHistory> secondSlice = historyRepository.findAllByUserIdAndSupplyType(
                user.getId(), supplyType, lastId, pageable);
        List<CreditHistory> content = secondSlice.getContent();

        for(int i = 0; i < content.size(); i++){
            assertThat(content.get(i).getSupplyType()).isEqualTo(supplyType);
        }
        assertThat(secondSlice.getContent()).hasSize(3);
        assertThat(secondSlice.getContent().get(0).getId()).isLessThan(lastId);
    }

    private SkillExchange createSkillExchange(User requester, User receiver, UserSkill receiverSkill) {
        return SkillExchange.create(
                requester,
                receiver,
                receiverSkill,
                LocalDate.of(2026, 1, 31),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                "message"
        );
    }

    private UserProfile createProfile(User user) {
        return UserProfile.create(
                user,
                null,
                ExchangeType.OFFLINE,
                null,
                null);
    }

    private UserSkill createUserSkill() {
        SkillCategory skillCategory = SkillCategory.create(SkillCategoryType.DEVELOPMENT);
        em.persist(skillCategory);
        return UserSkill.create(
                skillCategory,
                "java",
                "title",
                SkillProficiency.LOW,
                "description",
                30);
    }

    private CreditHistory createRewardHistory(int amount, int balanceAfter) {
        return CreditHistory.createReward(user, amount, balanceAfter, HistoryType.SIGNUP_REWARD);
    }

    private CreditHistory createExchangeHistory(SkillExchange skillExchange) {
        return CreditHistory.createSkillExchange(
                user,
                targetUser,
                skillExchange,
                SupplyType.USE,
                amount,
                balanceAfter,
                HistoryType.SIGNUP_REWARD
        );
    }

    private User createUser() {
        String uuid = UUID.randomUUID().toString();
        return User.create(
                OAuthProvider.KAKAO,
                "kakao" + uuid,
                uuid + "email@example.com",
                "name" + uuid,
                "https://image",
                "nickname" + uuid);
    }
}