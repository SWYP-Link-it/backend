package org.swyp.linkit.global.loadtest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import org.swyp.linkit.domain.user.entity.*;
import org.swyp.linkit.domain.user.repository.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(name = "loadtest.review-seeder.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ReviewStatSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager em;

    private static final int RECEIVER_COUNT = 1_000;
    private static final int REVIEWER_COUNT = 200;
    private static final int REVIEW_PER_SKILL = 200;  // 1,000 × 200 = 200,000건

    private static final int BATCH_SIZE = 1_000;
    private static final int FLUSH_EVERY = 100;

    // skill_exchange_id = NULL (Review.skillExchangeId nullable=true 적용 후 사용)
    private static final String REVIEW_SQL =
            "INSERT INTO review " +
                    "(skill_exchange_id, reviewee_skill_id, reviewer_id, reviewee_id, content, rating, created_at, modified_at) " +
                    "VALUES (NULL, ?, ?, ?, NULL, ?, NOW(), NOW())";

    private static final String USER_RATING_STAT_SQL =
            "INSERT INTO user_rating_stat " +
                    "(user_id, rating_sum, rating_count, created_at, modified_at) " +
                    "VALUES (?, ?, ?, NOW(), NOW())";

    private static final String USER_SKILL_RATING_STAT_SQL =
            "INSERT INTO user_skill_rating_stat " +
                    "(user_skill_id, rating_sum, rating_count, " +
                    "star1count, star2count, star3count, star4count, star5count, created_at, modified_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

    @Override
    public void run(String... args) {
        if (userRepository.existsByNickname("skill_detail_receiver1")) {
            log.info("[SkillDetailSeeder] 이미 데이터가 존재합니다. 건너뜁니다.");
            return;
        }

        log.info("[SkillDetailSeeder] ===== 스킬 상세 조회 성능 측정 데이터 생성 시작 =====");
        long startMs = System.currentTimeMillis();

        // 1. SkillCategory (DEVELOPMENT, 없으면 생성)
        SkillCategory category = skillCategoryRepository
                .findByCategoryType(SkillCategoryType.DEVELOPMENT)
                .orElseGet(() -> skillCategoryRepository.save(
                        SkillCategory.create(SkillCategoryType.DEVELOPMENT)));

        // 2. receiver 500명 (User + UserProfile + UserSkill 1개/명)
        List<Long> receiverIds = new ArrayList<>(RECEIVER_COUNT);
        List<Long> receiverSkillIds = new ArrayList<>(RECEIVER_COUNT);

        transactionTemplate.execute(s -> {
            for (int i = 1; i <= RECEIVER_COUNT; i++) {
                User user = userRepository.save(User.create(
                        OAuthProvider.KAKAO,
                        "sd_recv_oauth_" + i,
                        "skill_detail_receiver" + i + "@test.com",
                        "스킬상세수신자" + i,
                        null,
                        "skill_detail_receiver" + i));

                UserProfile profile = userProfileRepository.save(UserProfile.create(
                        user, "스킬 상세 테스트 수신자 " + i,
                        ExchangeType.ONLINE, PreferredRegion.SEOUL, null));
                user.completeProfile();

                // unique constraint: (user_profile_id, skill_category_id, skill_name)
                UserSkill skill = UserSkill.create(
                        category,
                        "sd_skill_" + i,
                        "스킬 상세 강의 " + i,
                        SkillProficiency.HIGH,
                        "전문 강의",
                        30);
                profile.addUserSkill(skill);
                skill = userSkillRepository.save(skill);

                receiverIds.add(user.getId());
                receiverSkillIds.add(skill.getId());

                if (i % FLUSH_EVERY == 0) {
                    em.flush();
                    em.clear();
                }
            }
            return null;
        });
        log.info("[SkillDetailSeeder] receiver {}명 완료", RECEIVER_COUNT);

        // 3. reviewer 200명 (User only)
        List<Long> reviewerIds = new ArrayList<>(REVIEWER_COUNT);

        transactionTemplate.execute(s -> {
            for (int i = 1; i <= REVIEWER_COUNT; i++) {
                User user = userRepository.save(User.create(
                        OAuthProvider.KAKAO,
                        "sd_revwr_oauth_" + i,
                        "skill_detail_reviewer" + i + "@test.com",
                        "스킬상세작성자" + i,
                        null,
                        "skill_detail_reviewer" + i));

                reviewerIds.add(user.getId());

                if (i % FLUSH_EVERY == 0) {
                    em.flush();
                    em.clear();
                }
            }
            return null;
        });
        log.info("[SkillDetailSeeder] reviewer {}명 완료", REVIEWER_COUNT);

        // 4. review 100,000건 배치 구성 + stat 집계 누적
        // stat 배열 구조: [ratingSum, ratingCount, star1, star2, star3, star4, star5]
        Map<Long, int[]> receiverStat = new LinkedHashMap<>();
        Map<Long, int[]> skillStat = new LinkedHashMap<>();
        for (int i = 0; i < RECEIVER_COUNT; i++) {
            receiverStat.put(receiverIds.get(i), new int[7]);
            skillStat.put(receiverSkillIds.get(i), new int[7]);
        }

        List<Object[]> reviewBatch = new ArrayList<>(RECEIVER_COUNT * REVIEW_PER_SKILL);
        int totalIdx = 0;

        for (int ri = 0; ri < RECEIVER_COUNT; ri++) {
            Long receiverId = receiverIds.get(ri);
            Long skillId = receiverSkillIds.get(ri);

            for (int rw = 0; rw < REVIEW_PER_SKILL; rw++) {
                Long reviewerId = reviewerIds.get(rw % REVIEWER_COUNT);
                int rating = resolveRating(totalIdx);

                // params 순서: reviewee_skill_id, reviewer_id, reviewee_id, rating
                reviewBatch.add(new Object[]{skillId, reviewerId, receiverId, rating});

                int[] rs = receiverStat.get(receiverId);
                int[] ss = skillStat.get(skillId);
                rs[0] += rating;
                rs[1]++;
                rs[rating + 1]++;
                ss[0] += rating;
                ss[1]++;
                ss[rating + 1]++;

                totalIdx++;
            }

            if (ri % 50 == 0) {
                log.info("[SkillDetailSeeder] review 배치 구성 중... {}/{} ({}건)",
                        ri, RECEIVER_COUNT, totalIdx);
            }
        }

        log.info("[SkillDetailSeeder] review 배치 구성 완료 — {}건", totalIdx);
        batchInsert(REVIEW_SQL, reviewBatch);
        log.info("[SkillDetailSeeder] review INSERT 완료");

        // 5. user_rating_stat 500건
        List<Object[]> ratingStatBatch = receiverStat.entrySet().stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()[0], e.getValue()[1]})
                .collect(Collectors.toList());
        batchInsert(USER_RATING_STAT_SQL, ratingStatBatch);
        log.info("[SkillDetailSeeder] user_rating_stat {}건 INSERT 완료", ratingStatBatch.size());

        // 6. user_skill_rating_stat 500건  (v: [sum, cnt, s1, s2, s3, s4, s5])
        List<Object[]> skillStatBatch = skillStat.entrySet().stream()
                .map(e -> {
                    int[] v = e.getValue();
                    return new Object[]{e.getKey(), v[0], v[1], v[2], v[3], v[4], v[5], v[6]};
                })
                .collect(Collectors.toList());
        batchInsert(USER_SKILL_RATING_STAT_SQL, skillStatBatch);
        log.info("[SkillDetailSeeder] user_skill_rating_stat {}건 INSERT 완료", skillStatBatch.size());

        // 7. user_skill.user_skill_rating_stat_id FK 연결
        jdbcTemplate.update(
                "UPDATE user_skill us " +
                        "INNER JOIN user_skill_rating_stat urs ON urs.user_skill_id = us.user_skill_id " +
                        "SET us.user_skill_rating_stat_id = urs.user_skill_rating_stat_id");
        log.info("[SkillDetailSeeder] user_skill FK 연결 완료");

        log.info("[SkillDetailSeeder] ===== 완료 ({}ms) | receiver={} | reviewer={} | review={} | userStat={} | skillStat={} =====",
                System.currentTimeMillis() - startMs,
                RECEIVER_COUNT, REVIEWER_COUNT, totalIdx,
                ratingStatBatch.size(), skillStatBatch.size());
    }

    /**
     * 평점 분포: 5점 40% / 4점 35% / 3점 15% / 2점 5% / 1점 5%
     */
    private int resolveRating(int idx) {
        int mod = idx % 20;
        if (mod < 8) return 5;
        if (mod < 15) return 4;
        if (mod < 18) return 3;
        if (mod < 19) return 2;
        return 1;
    }

    private void batchInsert(String sql, List<Object[]> batch) {
        if (batch.isEmpty()) return;
        for (int i = 0; i < batch.size(); i += BATCH_SIZE) {
            List<Object[]> chunk = batch.subList(i, Math.min(i + BATCH_SIZE, batch.size()));
            jdbcTemplate.batchUpdate(sql, chunk);
        }
    }
}
