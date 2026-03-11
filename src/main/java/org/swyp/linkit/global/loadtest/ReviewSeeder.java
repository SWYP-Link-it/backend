package org.swyp.linkit.global.loadtest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import org.swyp.linkit.domain.user.entity.*;
import org.swyp.linkit.domain.user.repository.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 리뷰 평점 조회 성능 비교 전용 데이터 시더
 * receiver 10명 × reviewer 5,000명 = 50,000건 COMPLETED 거래 + 50,000건 리뷰
 * → GET /reviews/received/rating-info 집계 테이블 vs 나이브 쿼리 성능 비교용
 * 활성화: REVIEW_SEEDER_ENABLED=true 환경변수
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "loadtest.review-seeder.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ReviewSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    private static final int RECEIVER_COUNT = 10;
    private static final int REVIEWER_COUNT = 5_000;
    private static final int EXCHANGE_DURATION = 30; // 30분 = 1 크레딧
    private static final int CREDIT_PRICE = 1;
    private static final int BATCH_SIZE = 1_000;

    // reviewer rw 번째는 BASE_DATE + rw * RECEIVER_COUNT 를 기준일로 사용
    // → 동일 receiver에 대해 날짜가 겹치지 않음
    private static final LocalDate BASE_DATE = LocalDate.of(2010, 1, 1);

    private static final LocalTime EXCHANGE_START = LocalTime.of(10, 0);
    private static final LocalTime EXCHANGE_END = LocalTime.of(10, 30);

    private static final String EXCHANGE_SQL =
            "INSERT INTO skill_exchange " +
                    "(requester_id, receiver_id, receiver_skill_id, skill_name, exchange_duration, " +
                    "scheduled_date, start_time, end_time, request_dead_line, credit_price, " +
                    "exchange_status, is_requester_read, is_receiver_read, message, created_at, modified_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false, false, null, NOW(), NOW())";

    private static final String REVIEW_SQL =
            "INSERT INTO review " +
                    "(skill_exchange_id, reviewee_skill_id, reviewer_id, reviewee_id, content, rating, created_at, modified_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";

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
        if (userRepository.existsByNickname("review_stat_receiver1")) {
            log.info("[ReviewSeeder] 이미 데이터가 존재합니다. 건너뜁니다.");
            return;
        }

        log.info("[ReviewSeeder] ===== 리뷰 평점 성능 비교 데이터 생성 시작 =====");
        long startMs = System.currentTimeMillis();

        // 1. SkillCategory (없으면 생성)
        SkillCategory category = skillCategoryRepository
                .findByCategoryType(SkillCategoryType.DEVELOPMENT)
                .orElseGet(() -> skillCategoryRepository.save(
                        SkillCategory.create(SkillCategoryType.DEVELOPMENT)));

        // 2. receiver 10명 생성 (User + UserProfile + UserSkill 1개)
        List<Long> receiverIds = new ArrayList<>(RECEIVER_COUNT);
        List<Long> receiverSkillIds = new ArrayList<>(RECEIVER_COUNT);
        List<String> receiverSkillNames = new ArrayList<>(RECEIVER_COUNT);

        transactionTemplate.execute(s -> {
            for (int i = 1; i <= RECEIVER_COUNT; i++) {
                User user = userRepository.save(User.create(
                        OAuthProvider.KAKAO,
                        "rs_recv_oauth_" + i,
                        "review_stat_receiver" + i + "@test.com",
                        "리뷰수신자" + i, null,
                        "review_stat_receiver" + i));

                UserProfile profile = userProfileRepository.save(UserProfile.create(
                        user, "리뷰 수신자 " + i, ExchangeType.ONLINE, PreferredRegion.SEOUL, null));
                user.completeProfile();

                String skillName = "receiver" + i + "_skill";
                UserSkill skill = UserSkill.create(
                        category, skillName, "수신자" + i + " 강의",
                        SkillProficiency.HIGH, "전문 강의", EXCHANGE_DURATION);
                profile.addUserSkill(skill);
                skill = userSkillRepository.save(skill);

                receiverIds.add(user.getId());
                receiverSkillIds.add(skill.getId());
                receiverSkillNames.add(skillName);
            }
            return null;
        });
        log.info("[ReviewSeeder] receiver {}명 완료", RECEIVER_COUNT);

        // 3. reviewer 5,000명 생성 (User only, 프로필 없음)
        List<Long> reviewerIds = new ArrayList<>(REVIEWER_COUNT);
        transactionTemplate.execute(s -> {
            for (int i = 1; i <= REVIEWER_COUNT; i++) {
                User user = userRepository.save(User.create(
                        OAuthProvider.KAKAO,
                        "rs_revwr_oauth_" + i,
                        "review_stat_reviewer" + i + "@test.com",
                        "리뷰작성자" + i, null,
                        "review_stat_reviewer" + i));
                reviewerIds.add(user.getId());
            }
            return null;
        });
        log.info("[ReviewSeeder] reviewer {}명 완료", REVIEWER_COUNT);

        // 4. COMPLETED 거래 50,000건 + 리뷰 50,000건 구성
        long baseId = queryMaxExchangeId();
        int total = REVIEWER_COUNT * RECEIVER_COUNT; // 50,000

        List<Object[]> exchangeBatch = new ArrayList<>(total);
        List<Object[]> reviewBatch = new ArrayList<>(total);

        // receiver / skill 별 평점 통계 누적: [sum, count, star1..star5]
        Map<Long, int[]> receiverRatingStat = new LinkedHashMap<>();
        Map<Long, int[]> skillRatingStat = new LinkedHashMap<>();
        for (int ri = 0; ri < RECEIVER_COUNT; ri++) {
            receiverRatingStat.put(receiverIds.get(ri), new int[7]);
            skillRatingStat.put(receiverSkillIds.get(ri), new int[7]);
        }

        int totalIdx = 0;
        for (int rw = 0; rw < REVIEWER_COUNT; rw++) {
            Long reviewerId = reviewerIds.get(rw);
            // reviewer마다 서로 다른 기준일 → 동일 receiver의 날짜 충돌 방지
            LocalDate reviewerBase = BASE_DATE.plusDays((long) rw * RECEIVER_COUNT);

            for (int ri = 0; ri < RECEIVER_COUNT; ri++) {
                Long receiverId = receiverIds.get(ri);
                Long skillId = receiverSkillIds.get(ri);
                String skillName = receiverSkillNames.get(ri);
                LocalDate date = reviewerBase.plusDays(ri);

                exchangeBatch.add(new Object[]{
                        reviewerId, receiverId, skillId,
                        skillName, EXCHANGE_DURATION,
                        java.sql.Date.valueOf(date),
                        java.sql.Time.valueOf(EXCHANGE_START),
                        java.sql.Time.valueOf(EXCHANGE_END),
                        Timestamp.valueOf(date.atStartOfDay()),
                        CREDIT_PRICE, "COMPLETED"
                });

                long exchangeId = baseId + totalIdx + 1;
                int rating = resolveRating(totalIdx);
                reviewBatch.add(new Object[]{exchangeId, skillId, reviewerId, receiverId, null, rating});

                int[] rs = receiverRatingStat.get(receiverId);
                int[] ss = skillRatingStat.get(skillId);
                rs[0] += rating;
                rs[1]++;
                rs[rating + 1]++;
                ss[0] += rating;
                ss[1]++;
                ss[rating + 1]++;

                totalIdx++;
            }
        }

        log.info("[ReviewSeeder] 배치 구성 완료 — exchange/review {}건", totalIdx);
        batchInsert(EXCHANGE_SQL, exchangeBatch);
        log.info("[ReviewSeeder] exchange INSERT 완료");
        batchInsert(REVIEW_SQL, reviewBatch);
        log.info("[ReviewSeeder] review INSERT 완료");

        // 5. UserRatingStat / UserSkillRatingStat 삽입
        List<Object[]> ratingStatBatch = receiverRatingStat.entrySet().stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()[0], e.getValue()[1]})
                .collect(Collectors.toList());
        batchInsert(USER_RATING_STAT_SQL, ratingStatBatch);

        List<Object[]> skillStatBatch = skillRatingStat.entrySet().stream()
                .map(e -> {
                    int[] v = e.getValue();
                    return new Object[]{e.getKey(), v[0], v[1], v[2], v[3], v[4], v[5], v[6]};
                })
                .collect(Collectors.toList());
        batchInsert(USER_SKILL_RATING_STAT_SQL, skillStatBatch);
        log.info("[ReviewSeeder] UserRatingStat {}건, UserSkillRatingStat {}건 INSERT 완료",
                ratingStatBatch.size(), skillStatBatch.size());

        // 6. user_skill.user_skill_rating_stat_id FK 연결
        //    (JdbcTemplate batch insert 후 JPA @GeneratedValue FK가 null 상태 → JOIN UPDATE로 연결)
        jdbcTemplate.update(
                "UPDATE user_skill us " +
                        "INNER JOIN user_skill_rating_stat urs ON urs.user_skill_id = us.user_skill_id " +
                        "SET us.user_skill_rating_stat_id = urs.user_skill_rating_stat_id");
        log.info("[ReviewSeeder] user_skill.user_skill_rating_stat_id FK 연결 완료");

        log.info("[ReviewSeeder] ===== 완료 ({}ms) | exchange={} review={} =====",
                System.currentTimeMillis() - startMs, totalIdx, totalIdx);
    }

    /**
     * 평점 분포: 5점 40%, 4점 35%, 3점 15%, 2점 5%, 1점 5%
     */
    private int resolveRating(int idx) {
        int mod = idx % 20;
        if (mod < 8) return 5;
        if (mod < 15) return 4;
        if (mod < 18) return 3;
        if (mod < 19) return 2;
        return 1;
    }

    private long queryMaxExchangeId() {
        Long max = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(skill_exchange_id), 0) FROM skill_exchange", Long.class);
        return max != null ? max : 0L;
    }

    private void batchInsert(String sql, List<Object[]> batch) {
        if (batch.isEmpty()) return;
        for (int i = 0; i < batch.size(); i += BATCH_SIZE) {
            List<Object[]> chunk = batch.subList(i, Math.min(i + BATCH_SIZE, batch.size()));
            jdbcTemplate.batchUpdate(sql, chunk);
        }
    }
}
