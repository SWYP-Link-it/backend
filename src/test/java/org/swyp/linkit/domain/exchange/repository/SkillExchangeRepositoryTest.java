package org.swyp.linkit.domain.exchange.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.user.entity.*;
import org.swyp.linkit.global.config.JpaAuditingConfig;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("SkillExchangeRepository 단위 테스트")
class SkillExchangeRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    SkillExchangeRepository exchangeRepository;

    private User receiver;
    private LocalDate date;

    @BeforeEach
    void setup(){
        // requester, receiver 세팅
        User requester = createUser();
        receiver = createUser();
        em.persist(requester);
        em.persist(receiver);

        // userSkill 세팅
        SkillCategory skillCategory1 = createSkillCategory(SkillCategoryType.DEVELOPMENT);
        SkillCategory skillCategory2 = createSkillCategory(SkillCategoryType.DESIGN);
        em.persist(skillCategory1);
        em.persist(skillCategory2);

        UserSkill requesterSkill = createUserSkill(skillCategory1);
        UserSkill receiverSkill = createUserSkill(skillCategory2);

        UserProfile requesterProfile = createProfile(requester, requesterSkill);
        UserProfile receiverProfile = createProfile(receiver, receiverSkill);
        em.persist(requesterProfile);
        em.persist(receiverProfile);
        em.persist(requesterSkill);
        em.persist(receiverSkill);

        date = LocalDate.of(2026, 2, 6);
        // skillExchange 세팅
        for(int i = 1; i <= 10; i+=2){
            SkillExchange exchange = createExchange(requester, receiver, receiverSkill,
                    date, LocalTime.of(i, 0), LocalTime.of(i + 1, 0));
            if(i == 1) exchange.updateExchangeStatus(ExchangeStatus.CANCELED);
            exchangeRepository.save(exchange);
        }

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("receiverId, date, exchangeStatus 로 SkillExchangeList 조회")
    public void findAllByReceiverIdAndDate(){
        //given
        Long receiverId = receiver.getId();
        ExchangeStatus status = ExchangeStatus.CANCELED;

        //when
        List<SkillExchange> sut = exchangeRepository.findAllByReceiverIdAndDate(receiverId, date, status);

        //then
        assertThat(sut.size()).isEqualTo(4);

        boolean hasCanceled = sut.stream().anyMatch(se -> se.getExchangeStatus() == status);
        assertThat(hasCanceled).isFalse();

        SkillExchange firstExchange = sut.get(0);
        assertEquals(receiverId, firstExchange.getReceiver().getId());
        assertEquals(date, firstExchange.getScheduledDate());
        assertNotNull(firstExchange.getReceiverSkill().getSkillName());
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

    private UserProfile createProfile(User user, UserSkill userSkill){
        UserProfile userProfile = UserProfile.create(
                user,
                "introduction",
                "description",
                ExchangeType.OFFLINE,
                null,
                null);
        userProfile.addUserSkill(userSkill);
        return userProfile;
    }
    private SkillCategory createSkillCategory(SkillCategoryType type){
        return SkillCategory.create(type);
    }

    private UserSkill createUserSkill(SkillCategory type){
        return UserSkill.create(
                type,
                "skillName",
                SkillLevel.HIGH,
                "description",
                30,
                true);
    }

    private SkillExchange createExchange(User requester, User receiver, UserSkill receiverSkill,
                                         LocalDate scheduledDate, LocalTime start, LocalTime end){
        return SkillExchange.create(
                requester,
                receiver,
                receiverSkill,
                scheduledDate,
                start,
                end,
                "defaultMessage");
    }


}