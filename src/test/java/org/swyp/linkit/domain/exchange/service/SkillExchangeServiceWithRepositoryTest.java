package org.swyp.linkit.domain.exchange.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.swyp.linkit.TestRedisConfig;
import org.swyp.linkit.domain.exchange.dto.SkillExchangeDto;
import org.swyp.linkit.domain.exchange.dto.request.SkillExchangeRequestDto;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.user.entity.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestRedisConfig.class)
@ActiveProfiles("test")
@SpringBootTest
@DisplayName("SkillExchangeService + Repository 테스트")
public class SkillExchangeServiceWithRepositoryTest {

    @Autowired
    private PlatformTransactionManager transactionManager;
    ;

    @Autowired
    private EntityManager em;

    @Autowired
    private SkillExchangeService exchangeService;

    private LocalTime startTime = LocalTime.of(10, 0);
    private LocalTime endTime = LocalTime.of(11, 0);
    private User mentee;
    private User mentor;
    private UserSkill mentorSkill;

    @BeforeEach
    void setup() {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            // 멘토, 멘티 생성
            mentee = createUser();
            mentor = createUser();
            em.persist(mentee);
            em.persist(mentor);

            // 스킬 카테고리 생성
            SkillCategory skillCategory = createSkillCategory();
            em.persist(skillCategory);

            // 멘토 스킬 생성
            mentorSkill = createUserSkill(skillCategory, 60);
            UserProfile mentorProfile = createUserProfile(mentor, List.of(mentorSkill));
            em.persist(mentorProfile);
            em.persist(mentorSkill);

            // 멘토 가능 날짜 및 시간 생성
            // 일 ~ 월 [10:00 ~ 12:00]
            List<AvailableSchedule> availableSchedules = createAvailableSchedules(
                    mentor, LocalTime.of(10, 0), LocalTime.of(12, 0));
            for (AvailableSchedule availableSchedule : availableSchedules) {
                em.persist(availableSchedule);
            }

            // 스킬 교환 생성
            SkillExchange exchange = createExchange(mentor, mentee, mentorSkill, startTime, endTime);
            em.persist(exchange);

            em.flush();
            transactionManager.commit(status);
            em.clear();
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }

    }

    @Test
    @DisplayName("스킬 거래 요청 동시성 테스트")
    public void requestSkillExchange_concurrency() throws InterruptedException {
        // given
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        SkillExchangeRequestDto requestDto = new SkillExchangeRequestDto(mentor.getId(), mentorSkill.getId(),
                "메시지", LocalDate.now().plusDays(2), startTime);
        SkillExchangeDto skillExchangeDto = SkillExchangeDto.from(requestDto);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    exchangeService.requestSkillExchange(mentee.getId(), skillExchangeDto);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("실패 원인: " + e.getClass());
                    System.out.println("실패 원인: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);

    }

    private List<AvailableSchedule> createAvailableSchedules(User user, LocalTime start, LocalTime end) {
        return List.of(
                AvailableSchedule.create(user, Weekday.MON, start, end),
                AvailableSchedule.create(user, Weekday.TUE, start, end),
                AvailableSchedule.create(user, Weekday.WED, start, end),
                AvailableSchedule.create(user, Weekday.THU, start, end),
                AvailableSchedule.create(user, Weekday.FRI, start, end),
                AvailableSchedule.create(user, Weekday.SAT, start, end),
                AvailableSchedule.create(user, Weekday.SUN, start, end)
        );
    }

    private UserProfile createUserProfile(User user, List<UserSkill> userSkill) {
        UserProfile userProfile = UserProfile.create(user,
                "introduction",
                "description",
                ExchangeType.OFFLINE,
                PreferredRegion.CHUNGCHEONG,
                "location");
        for (UserSkill skill : userSkill) {
            userProfile.addUserSkill(skill);
        }
        return userProfile;
    }

    private User createUser() {
        String uuid = UUID.randomUUID().toString();
        return User.create(
                OAuthProvider.KAKAO,
                "kakao" + uuid,
                "email@example.com" + uuid,
                "name" + uuid,
                "https://image",
                "nickname" + uuid);
    }

    private UserSkill createUserSkill(SkillCategory skillCategory, int exchangeDuration) {
        return UserSkill.create(
                skillCategory,
                "skillName",
                SkillLevel.LOW,
                "description",
                exchangeDuration,
                true);
    }

    private UserSkill createUnVisibleUserSkill(SkillCategory skillCategory, int exchangeDuration) {
        return UserSkill.create(
                skillCategory,
                "skillName",
                SkillLevel.LOW,
                "description",
                exchangeDuration,
                false);
    }

    private SkillCategory createSkillCategory() {
        return SkillCategory.create(SkillCategoryType.DEVELOPMENT);
    }

    private SkillExchange createExchange(User receiverUser, User requester, UserSkill receiverSkill,
                                         LocalTime starTime, LocalTime endTime) {

        return SkillExchange.create(
                requester, receiverUser, receiverSkill, LocalDate.of(2026, 2, 4),
                starTime, endTime, "message");
    }
}
