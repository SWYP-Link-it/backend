//package org.swyp.linkit.global.loadtest;
//
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import org.swyp.linkit.domain.credit.entity.Credit;
//import org.swyp.linkit.domain.credit.entity.CreditHistory;
//import org.swyp.linkit.domain.credit.entity.HistoryType;
//import org.swyp.linkit.domain.credit.repository.CreditHistoryRepository;
//import org.swyp.linkit.domain.credit.repository.CreditRepository;
//import org.swyp.linkit.domain.exchange.repository.SkillExchangeRepository;
//import org.swyp.linkit.domain.review.repository.ReviewRepository;
//import org.swyp.linkit.domain.settlement.repository.SettlementRepository;
//import org.swyp.linkit.domain.user.entity.*;
//import org.swyp.linkit.domain.user.repository.*;
//
//import java.sql.Timestamp;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * 부하 테스트용 더미 데이터 시더 (30,000명 규모)
// * <p>
// * 성능 개선 3가지 수치 측정을 위한 데이터:
// * #1  POST /exchange/request — 비관적 락 범위 축소 (k6 VU 20)
// * #2  GET  /credits/histories — 인덱스 + DTO Projection (k6 VU 30)
// * #3  processAutoSettlement() — 2N→N 쿼리 (P6spy)
// *
// * 유저 그룹:
// * mentor      2명     | 날짜·슬롯 조회 전용
// * requester 200명     | k6 동시성 VU
// * counterpart 500명   | 거래 수신자 풀
// * paging_user 300명   | 크레딧 내역 페이징 테스트
// * general  28,998명   | 인덱스·마켓 bulk 데이터
// *
// * 전체 데이터:
// * User 30,000 | UserSkill 50,000 | Exchange 22,000
// * Settlement 19,000 | Review 9,000 | CreditHistory ~86,000
// */
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class LoadTestDataSeeder implements CommandLineRunner {
//
//    private final SkillCategoryRepository skillCategoryRepository;
//    private final UserRepository userRepository;
//    private final UserProfileRepository userProfileRepository;
//    private final UserSkillRepository userSkillRepository;
//    private final AvailableScheduleRepository availableScheduleRepository;
//    private final CreditRepository creditRepository;
//    private final CreditHistoryRepository creditHistoryRepository;
//    private final SkillExchangeRepository skillExchangeRepository;
//    private final SettlementRepository settlementRepository;
//    private final ReviewRepository reviewRepository;
//    private final JdbcTemplate jdbcTemplate;
//
//    @PersistenceContext
//    private EntityManager em;
//
//    // 총 user 수 = 30,000
//    // == 그룹 규모 ==
//    private static final int MENTOR_COUNT = 2;
//    // 스킬 거래 요청 rqeuester
//    private static final int REQUESTER_COUNT = 200;
//    // 스킬 거래 요청 receiver
//    private static final int COUNTERPART_COUNT = 500;
//    // 크레딧 내역 페이징
//    private static final int PAGING_USER_COUNT = 300;
//    private static final int GENERAL_COUNT = 28_998;
//    private static final int GENERAL_WITH_SKILL = 24_198;
//
//    // == 스킬 거래 규모 ==
//    private static final int ACCEPTED_COUNT = 10_000;
//    private static final int COMPLETED_PER_PAGING = 30;     // paging_user 1명당 30건
//    private static final int PENDING_COUNT = 1_500;
//    private static final int REJECTED_COUNT = 1_000;
//    private static final int CANCELED_COUNT = 500;
//
//    //== 날짜 기준 ==
//    private static final LocalDate ACCEPTED_DATE_BASE = LocalDate.of(2025, 1, 1);
//    private static final LocalDate COMPLETED_DATE_BASE = LocalDate.of(2024, 6, 1);
//    private static final LocalDate PENDING_DATE_BASE = LocalDate.of(2026, 4, 1);
//    private static final LocalDate PAST_DATE_BASE = LocalDate.of(2025, 6, 1);
//    private static final LocalTime EXCHANGE_START = LocalTime.of(10, 0);
//    private static final LocalTime EXCHANGE_END = LocalTime.of(10, 30);
//
//    // == batch 설정 ==
//    private static final int FLUSH_EVERY = 100;
//    private static final int BATCH_SIZE = 1_000;
//
//    // == credit ==
//    private static final int INITIAL_CREDIT = 200;
//    private static final int SIGNUP_REWARD_AMT = 2;
//    private static final int PROFILE_REWARD_AMT = 1;
//
//    // == JdbcTemplate SQL ==
//    private static final String EXCHANGE_SQL =
//            "INSERT INTO skill_exchange " +
//                    "(requester_id, receiver_id, receiver_skill_id, skill_name, exchange_duration, " +
//                    "scheduled_date, start_time, end_time, request_dead_line, credit_price, " +
//                    "exchange_status, is_requester_read, is_receiver_read, message, created_at, modified_at) " +
//                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false, false, null, NOW(), NOW())";
//
//    private static final String SETTLEMENT_SQL =
//            "INSERT INTO settlement " +
//                    "(skill_exchange_id, receiver_id, amount, status, settled_at, created_at, modified_at) " +
//                    "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
//
//    private static final String CREDIT_HISTORY_SQL =
//            "INSERT INTO credit_history " +
//                    "(user_id, target_user_id, skill_exchange_id, history_type, supply_type, " +
//                    "change_amount, balance_after, content_name, created_at, modified_at) " +
//                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
//
//    private static final String REVIEW_SQL =
//            "INSERT INTO review " +
//                    "(skill_exchange_id, reviewee_skill_id, reviewer_id, reviewee_id, content, rating, created_at, modified_at) " +
//                    "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
//
//    private static final String USER_RATING_STAT_SQL =
//            "INSERT INTO user_rating_stat " +
//                    "(user_id, rating_sum, rating_count, created_at, modified_at) " +
//                    "VALUES (?, ?, ?, NOW(), NOW())";
//
//    private static final String USER_SKILL_RATING_STAT_SQL =
//            "INSERT INTO user_skill_rating_stat " +
//                    "(user_skill_id, rating_sum, rating_count, " +
//                    "star1count, star2count, star3count, star4count, star5count, created_at, modified_at) " +
//                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
//
//    @Override
//    @Transactional
//    public void run(String... args) {
//        if (userRepository.existsByNickname("mentor1")) {
//            log.info("[Seeder] 이미 데이터가 존재합니다. 건너뜁니다.");
//            return;
//        }
//
//        log.info("[Seeder] ===== 부하 테스트 데이터 생성 시작 (목표: 30,000명) =====");
//        long startMs = System.currentTimeMillis();
//
//        // 1. SkillCategory (9개)
//        List<SkillCategory> categories = createCategories();
//
//        // 2. mentor 2명 (스킬 2개/명, AvailableSchedule MON~FRI)
//        createMentors(categories);
//
//        // 3. requester 200명 (스킬 없음, k6 VU)
//        createRequesters();
//
//        // 4. counterpart 500명 (스킬 2개/명, AvailableSchedule 있음)
//        List<Long> counterpartUserIds = new ArrayList<>(COUNTERPART_COUNT);
//        List<Long> counterpartMainSkillIds = new ArrayList<>(COUNTERPART_COUNT);
//        List<String> counterpartSkillNames = new ArrayList<>(COUNTERPART_COUNT);
//        List<Integer> counterpartSkillDurations = new ArrayList<>(COUNTERPART_COUNT);
//        createCounterparts(categories, counterpartUserIds, counterpartMainSkillIds,
//                counterpartSkillNames, counterpartSkillDurations);
//
//        // 5. paging_user 300명 (스킬 2개/명, COMPLETED 거래 페이징 테스트)
//        List<Long> pagingUserIds = new ArrayList<>(PAGING_USER_COUNT);
//        createPagingUsers(categories, pagingUserIds);
//
//        em.flush();
//        em.clear();
//
//        // 6. 28,998명 (24,198명 스킬 2개 보유)
//        List<Long> generalUserIds = new ArrayList<>(GENERAL_COUNT);
//        createGeneralUsers(categories, generalUserIds);
//
//        em.flush();
//        em.clear();
//
//        // 7. ACCEPTED 거래 10,000건 → Settlement PENDING + CreditHistory USE
//        createAcceptedExchanges(generalUserIds, counterpartUserIds,
//                counterpartMainSkillIds, counterpartSkillNames, counterpartSkillDurations);
//
//        // 8. COMPLETED 거래 9,000건 → Settlement COMPLETED + CreditHistory + Review + RatingStat
//        createCompletedExchanges(pagingUserIds, counterpartUserIds,
//                counterpartMainSkillIds, counterpartSkillNames, counterpartSkillDurations);
//
//        // 9. 기타 거래 3,000건 (PENDING 1,500 + REJECTED 1,000 + CANCELED 500)
//        createOtherExchanges(generalUserIds, counterpartUserIds,
//                counterpartMainSkillIds, counterpartSkillNames, counterpartSkillDurations);
//
//        log.info("[Seeder] ===== 완료 ({}ms) =====", System.currentTimeMillis() - startMs);
//        log.info("[Seeder] User={} | UserSkill={} | Exchange={} | Settlement={} | Review={}",
//                userRepository.count(), userSkillRepository.count(),
//                skillExchangeRepository.count(), settlementRepository.count(), reviewRepository.count());
//    }
//
//    /**
//     * skillCategory
//     */
//    private List<SkillCategory> createCategories() {
//        List<SkillCategory> list = Arrays.stream(SkillCategoryType.values())
//                .map(type -> skillCategoryRepository.save(SkillCategory.create(type)))
//                .collect(Collectors.toList());
//        log.info("[Seeder] SkillCategory {}개", list.size());
//        return list;
//    }
//
//    /**
//     * mentor 2명
//     */
//    private void createMentors(List<SkillCategory> categories) {
//        for (int i = 1; i <= MENTOR_COUNT; i++) {
//            User user = userRepository.save(User.create(
//                    OAuthProvider.KAKAO, "mentor_oauth_" + i,
//                    "mentor" + i + "@test.com", "멘토" + i, null, "mentor" + i));
//            UserProfile profile = userProfileRepository.save(
//                    UserProfile.create(user, "전문 멘토입니다", ExchangeType.BOTH, PreferredRegion.SEOUL, null));
//            user.completeProfile();
//
//            SkillCategory cat1 = categories.get((i - 1) % categories.size());
//            SkillCategory cat2 = categories.get((i + 1) % categories.size());
//
//            UserSkill skill1 = UserSkill.create(cat1, "mentor" + i + "_A",
//                    "멘토" + i + " 30분 강의", SkillProficiency.HIGH, "전문 강의", 30);
//            profile.addUserSkill(skill1);
//            userSkillRepository.save(skill1);
//
//            UserSkill skill2 = UserSkill.create(cat2, "mentor" + i + "_B",
//                    "멘토" + i + " 60분 강의", SkillProficiency.HIGH, "심화 강의", 60);
//            profile.addUserSkill(skill2);
//            userSkillRepository.save(skill2);
//
//            // AvailableSchedule: MON ~ FRI 09:00 ~ 18:00
//            for (Weekday day : new Weekday[]{Weekday.MON, Weekday.TUE, Weekday.WED, Weekday.THU, Weekday.FRI}) {
//                availableScheduleRepository.save(
//                        AvailableSchedule.create(user, day, LocalTime.of(9, 0), LocalTime.of(18, 0)));
//            }
//
//            creditRepository.save(Credit.create(user, INITIAL_CREDIT));
//            int bal = INITIAL_CREDIT + SIGNUP_REWARD_AMT;
//            creditHistoryRepository.save(CreditHistory.createReward(user, SIGNUP_REWARD_AMT, bal, HistoryType.SIGNUP_REWARD));
//            bal += PROFILE_REWARD_AMT;
//            creditHistoryRepository.save(CreditHistory.createReward(user, PROFILE_REWARD_AMT, bal, HistoryType.PROFILE_REWARD));
//        }
//        log.info("[Seeder] mentor {}명 완료", MENTOR_COUNT);
//    }
//
//    /**
//     * requester 200명 (스킬 없음)
//     */
//    private void createRequesters() {
//        PreferredRegion[] regions = PreferredRegion.values();
//        for (int i = 1; i <= REQUESTER_COUNT; i++) {
//            User user = userRepository.save(User.create(
//                    OAuthProvider.KAKAO, "req_oauth_" + i,
//                    "requester" + i + "@test.com", "요청자" + i, null, "requester" + i));
//            userProfileRepository.save(UserProfile.create(
//                    user, "교환 희망", ExchangeType.BOTH, regions[i % regions.length], null));
//            user.completeProfile();
//
//            creditRepository.save(Credit.create(user, INITIAL_CREDIT));
//            int bal = INITIAL_CREDIT + SIGNUP_REWARD_AMT;
//            creditHistoryRepository.save(CreditHistory.createReward(user, SIGNUP_REWARD_AMT, bal, HistoryType.SIGNUP_REWARD));
//
//            if (i % FLUSH_EVERY == 0) {
//                em.flush();
//                em.clear();
//            }
//        }
//        log.info("[Seeder] requester {}명 완료", REQUESTER_COUNT);
//    }
//
//    /**
//     * counterpart 500명 (스킬 2개/명, AvailableSchedule 있음)
//     */
//    private void createCounterparts(List<SkillCategory> categories,
//                                    List<Long> userIds, List<Long> mainSkillIds,
//                                    List<String> skillNames, List<Integer> skillDurations) {
//        PreferredRegion[] regions = PreferredRegion.values();
//        Weekday[] weekdays = {Weekday.MON, Weekday.TUE, Weekday.WED, Weekday.THU, Weekday.FRI};
//
//        for (int i = 1; i <= COUNTERPART_COUNT; i++) {
//            User user = userRepository.save(User.create(
//                    OAuthProvider.KAKAO, "cp_oauth_" + i,
//                    "counterpart" + i + "@test.com", "상대방" + i, null, "counterpart" + i));
//            UserProfile profile = userProfileRepository.save(
//                    UserProfile.create(user, "스킬 보유 전문가", ExchangeType.BOTH,
//                            regions[i % regions.length], null));
//            user.completeProfile();
//
//            // 주력 스킬 30분 — ACCEPTED/COMPLETED 거래의 receiverSkill
//            SkillCategory cat1 = categories.get((i - 1) % categories.size());
//            SkillCategory cat2 = categories.get(i % categories.size());
//
//            UserSkill mainSkill = UserSkill.create(cat1, "cp" + i + "_main",
//                    "상대방" + i + " 주력 강의", SkillProficiency.HIGH, "전문 강의", 30);
//            profile.addUserSkill(mainSkill);
//            mainSkill = userSkillRepository.save(mainSkill);
//
//            UserSkill subSkill = UserSkill.create(cat2, "cp" + i + "_sub",
//                    "상대방" + i + " 보조 강의", SkillProficiency.MEDIUM, "기초 강의", 60);
//            profile.addUserSkill(subSkill);
//            userSkillRepository.save(subSkill);
//
//            // AvailableSchedule: 주 2~5일 (MON~FRI 중)
//            int dayCount = 2 + (i % 4); // 2, 3, 4, 5 순환
//            for (int d = 0; d < dayCount; d++) {
//                availableScheduleRepository.save(
//                        AvailableSchedule.create(user, weekdays[(i + d) % 5],
//                                LocalTime.of(9, 0), LocalTime.of(21, 0)));
//            }
//
//            creditRepository.save(Credit.create(user, INITIAL_CREDIT));
//            int bal = INITIAL_CREDIT + SIGNUP_REWARD_AMT;
//            creditHistoryRepository.save(CreditHistory.createReward(user, SIGNUP_REWARD_AMT, bal, HistoryType.SIGNUP_REWARD));
//            bal += PROFILE_REWARD_AMT;
//            creditHistoryRepository.save(CreditHistory.createReward(user, PROFILE_REWARD_AMT, bal, HistoryType.PROFILE_REWARD));
//
//            userIds.add(user.getId());
//            mainSkillIds.add(mainSkill.getId());
//            skillNames.add(mainSkill.getSkillName());
//            skillDurations.add(mainSkill.getExchangeDuration()); // 30
//
//            if (i % FLUSH_EVERY == 0) {
//                em.flush();
//                em.clear();
//            }
//        }
//        log.info("[Seeder] counterpart {}명 완료", COUNTERPART_COUNT);
//    }
//
//    /**
//     * paging_user 300명
//     */
//    private void createPagingUsers(List<SkillCategory> categories, List<Long> userIds) {
//        PreferredRegion[] regions = PreferredRegion.values();
//        for (int i = 1; i <= PAGING_USER_COUNT; i++) {
//            User user = userRepository.save(User.create(
//                    OAuthProvider.KAKAO, "pu_oauth_" + i,
//                    "paging" + i + "@test.com", "페이징유저" + i, null, "paging_user" + i));
//            UserProfile profile = userProfileRepository.save(
//                    UserProfile.create(user, "페이징 테스트 유저", ExchangeType.BOTH,
//                            regions[i % regions.length], null));
//            user.completeProfile();
//
//            SkillCategory cat1 = categories.get((i - 1) % categories.size());
//            SkillCategory cat2 = categories.get((i + 2) % categories.size());
//
//            UserSkill skill1 = UserSkill.create(cat1, "pu" + i + "_A",
//                    "페이징유저" + i + " 강의 A", SkillProficiency.MEDIUM, "강의", 30);
//            profile.addUserSkill(skill1);
//            userSkillRepository.save(skill1);
//
//            UserSkill skill2 = UserSkill.create(cat2, "pu" + i + "_B",
//                    "페이징유저" + i + " 강의 B", SkillProficiency.LOW, "기초 강의", 60);
//            profile.addUserSkill(skill2);
//            userSkillRepository.save(skill2);
//
//            for (Weekday day : new Weekday[]{Weekday.MON, Weekday.WED, Weekday.FRI}) {
//                availableScheduleRepository.save(
//                        AvailableSchedule.create(user, day, LocalTime.of(9, 0), LocalTime.of(18, 0)));
//            }
//
//            creditRepository.save(Credit.create(user, INITIAL_CREDIT));
//            int bal = INITIAL_CREDIT + SIGNUP_REWARD_AMT;
//            creditHistoryRepository.save(CreditHistory.createReward(user, SIGNUP_REWARD_AMT, bal, HistoryType.SIGNUP_REWARD));
//            bal += PROFILE_REWARD_AMT;
//            creditHistoryRepository.save(CreditHistory.createReward(user, PROFILE_REWARD_AMT, bal, HistoryType.PROFILE_REWARD));
//
//            userIds.add(user.getId());
//
//            if (i % FLUSH_EVERY == 0) {
//                em.flush();
//                em.clear();
//            }
//        }
//        log.info("[Seeder] paging_user {}명 완료", PAGING_USER_COUNT);
//    }
//
//    /**
//     * general 28,998명
//     */
//    private void createGeneralUsers(List<SkillCategory> categories, List<Long> userIds) {
//        PreferredRegion[] regions = PreferredRegion.values();
//        ExchangeType[] types = ExchangeType.values();
//        SkillProficiency[] profs = SkillProficiency.values();
//
//        for (int i = 1; i <= GENERAL_COUNT; i++) {
//            User user = userRepository.save(User.create(
//                    OAuthProvider.KAKAO, "gen_oauth_" + i,
//                    "user" + i + "@test.com", "일반유저" + i, null, "user_" + i));
//            UserProfile profile = userProfileRepository.save(
//                    UserProfile.create(user, "교환 희망", types[i % types.length],
//                            regions[i % regions.length], null));
//            user.completeProfile();
//
//            creditRepository.save(Credit.create(user, INITIAL_CREDIT));
//            int bal = INITIAL_CREDIT + SIGNUP_REWARD_AMT;
//            creditHistoryRepository.save(CreditHistory.createReward(user, SIGNUP_REWARD_AMT, bal, HistoryType.SIGNUP_REWARD));
//
//            // 처음 GENERAL_WITH_SKILL (24,198)명만 스킬 2개 부여
//            if (i <= GENERAL_WITH_SKILL) {
//                SkillCategory cat1 = categories.get((i - 1) % categories.size());
//                SkillCategory cat2 = categories.get((i + 3) % categories.size());
//
//                UserSkill skill1 = UserSkill.create(cat1, "gen" + i + "_A",
//                        "일반유저" + i + " 강의 A", profs[i % 3], "강의", 30);
//                profile.addUserSkill(skill1);
//                userSkillRepository.save(skill1);
//
//                UserSkill skill2 = UserSkill.create(cat2, "gen" + i + "_B",
//                        "일반유저" + i + " 강의 B", profs[(i + 1) % 3], "강의", 60);
//                profile.addUserSkill(skill2);
//                userSkillRepository.save(skill2);
//
//                bal += PROFILE_REWARD_AMT;
//                creditHistoryRepository.save(CreditHistory.createReward(user, PROFILE_REWARD_AMT, bal, HistoryType.PROFILE_REWARD));
//            }
//
//            userIds.add(user.getId());
//
//            if (i % FLUSH_EVERY == 0) {
//                em.flush();
//                em.clear();
//            }
//        }
//        log.info("[Seeder] general {}명 완료 (스킬 보유: {}명)", GENERAL_COUNT, GENERAL_WITH_SKILL);
//    }
//
//
//    /**
//     * ACCEPTED 거래 10,000건
//     * requester: general user_1 ~ user_10,000 (각 1건)
//     * receiver:  counterpart 500명 (순환)
//     * → Settlement PENDING 10,000건
//     * → CreditHistory EXCHANGE_REQUEST USE 10,000건
//     */
//    private void createAcceptedExchanges(List<Long> generalUserIds,
//                                         List<Long> counterpartUserIds,
//                                         List<Long> cpSkillIds,
//                                         List<String> cpSkillNames,
//                                         List<Integer> cpSkillDurations) {
//
//        long baseId = queryMaxExchangeId();
//
//        List<Object[]> exchangeBatch = new ArrayList<>(ACCEPTED_COUNT);
//        List<Object[]> settlementBatch = new ArrayList<>(ACCEPTED_COUNT);
//        List<Object[]> creditHistoryBatch = new ArrayList<>(ACCEPTED_COUNT);
//
//        for (int i = 0; i < ACCEPTED_COUNT; i++) {
//            int cpIdx = i % COUNTERPART_COUNT;
//
//            Long requesterId = generalUserIds.get(i);
//            Long receiverId = counterpartUserIds.get(cpIdx);
//            Long skillId = cpSkillIds.get(cpIdx);
//            String skillName = cpSkillNames.get(cpIdx);
//            int duration = cpSkillDurations.get(cpIdx); // 30
//            int creditPrice = duration / 30;               // 1
//
//            LocalDate date = ACCEPTED_DATE_BASE.plusDays(i % 365);
//            LocalDateTime deadline = date.atStartOfDay();
//
//            exchangeBatch.add(new Object[]{
//                    requesterId, receiverId, skillId,
//                    skillName, duration,
//                    java.sql.Date.valueOf(date),
//                    java.sql.Time.valueOf(EXCHANGE_START),
//                    java.sql.Time.valueOf(EXCHANGE_END),
//                    Timestamp.valueOf(deadline),
//                    creditPrice, "ACCEPTED"
//            });
//
//            long exchangeId = baseId + i + 1;
//
//            // Settlement PENDING
//            settlementBatch.add(new Object[]{exchangeId, receiverId, creditPrice, "PENDING", null});
//
//            // CreditHistory: requester USE
//            creditHistoryBatch.add(new Object[]{
//                    requesterId, receiverId, exchangeId,
//                    "EXCHANGE_REQUEST", "USE", creditPrice,
//                    INITIAL_CREDIT - creditPrice, skillName
//            });
//        }
//
//        batchInsert(EXCHANGE_SQL, exchangeBatch);
//        batchInsert(SETTLEMENT_SQL, settlementBatch);
//        batchInsert(CREDIT_HISTORY_SQL, creditHistoryBatch);
//
//        log.info("[Seeder] ACCEPTED 거래 {}건 | Settlement PENDING {}건 | CreditHistory USE {}건",
//                ACCEPTED_COUNT, settlementBatch.size(), creditHistoryBatch.size());
//    }
//
//    /**
//     * COMPLETED 거래 9,000건
//     * requester: paging_user 300명 × 30건
//     * receiver:  counterpart 500명 (순환)
//     * → Settlement COMPLETED 9,000건
//     * → CreditHistory USE(requester) + ADD(receiver) 각 9,000건
//     * → Review 9,000건
//     * → UserRatingStat / UserSkillRatingStat 집계
//     */
//    private void createCompletedExchanges(List<Long> pagingUserIds,
//                                          List<Long> counterpartUserIds,
//                                          List<Long> cpSkillIds,
//                                          List<String> cpSkillNames,
//                                          List<Integer> cpSkillDurations) {
//
//        int total = PAGING_USER_COUNT * COMPLETED_PER_PAGING; // 9,000
//        long baseId = queryMaxExchangeId();
//
//        List<Object[]> exchangeBatch = new ArrayList<>(total);
//        List<Object[]> settlementBatch = new ArrayList<>(total);
//        List<Object[]> reviewBatch = new ArrayList<>(total);
//        List<Object[]> creditHistoryBatch = new ArrayList<>(total * 2);
//
//        // receiver별 평점 통계: [ratingSum, ratingCount, star1, star2, star3, star4, star5]
//        Map<Long, int[]> receiverRatingStat = new LinkedHashMap<>();
//        Map<Long, int[]> skillRatingStat = new LinkedHashMap<>();
//
//        int globalIdx = 0;
//        for (int i = 0; i < PAGING_USER_COUNT; i++) {
//            Long requesterId = pagingUserIds.get(i);
//            for (int j = 0; j < COMPLETED_PER_PAGING; j++) {
//                int cpIdx = (i + j) % COUNTERPART_COUNT;
//                Long receiverId = counterpartUserIds.get(cpIdx);
//                Long skillId = cpSkillIds.get(cpIdx);
//                String skillName = cpSkillNames.get(cpIdx);
//                int duration = cpSkillDurations.get(cpIdx); // 30
//                int creditPrice = duration / 30;               // 1
//
//                LocalDate date = COMPLETED_DATE_BASE.plusDays(globalIdx % 210);
//                LocalDateTime deadline = date.atStartOfDay();
//                Timestamp settledAt = Timestamp.valueOf(date.plusDays(1).atTime(10, 0));
//
//                exchangeBatch.add(new Object[]{
//                        requesterId, receiverId, skillId,
//                        skillName, duration,
//                        java.sql.Date.valueOf(date),
//                        java.sql.Time.valueOf(EXCHANGE_START),
//                        java.sql.Time.valueOf(EXCHANGE_END),
//                        Timestamp.valueOf(deadline),
//                        creditPrice, "COMPLETED"
//                });
//
//                long exchangeId = baseId + globalIdx + 1;
//
//                // Settlement COMPLETED
//                settlementBatch.add(new Object[]{exchangeId, receiverId, creditPrice, "COMPLETED", settledAt});
//
//                // CreditHistory: requester USE
//                creditHistoryBatch.add(new Object[]{
//                        requesterId, receiverId, exchangeId,
//                        "EXCHANGE_REQUEST", "USE", creditPrice,
//                        INITIAL_CREDIT - creditPrice, skillName
//                });
//                // CreditHistory: receiver ADD
//                creditHistoryBatch.add(new Object[]{
//                        receiverId, requesterId, exchangeId,
//                        "EXCHANGE_SETTLED", "ADD", creditPrice,
//                        INITIAL_CREDIT + creditPrice, skillName
//                });
//
//                // Review (평점 분포: 5점 40%, 4점 35%, 3점 15%, 2점 5%, 1점 5%)
//                int rating = resolveRating(globalIdx);
//                reviewBatch.add(new Object[]{exchangeId, skillId, requesterId, receiverId, null, rating});
//
//                // 통계 집계 (receiverId, skillId 기준)
//                int[] rs = receiverRatingStat.computeIfAbsent(receiverId, k -> new int[7]);
//                int[] ss = skillRatingStat.computeIfAbsent(skillId, k -> new int[7]);
//                rs[0] += rating;
//                rs[1]++;
//                rs[rating + 1]++;
//                ss[0] += rating;
//                ss[1]++;
//                ss[rating + 1]++;
//
//                globalIdx++;
//            }
//        }
//
//        batchInsert(EXCHANGE_SQL, exchangeBatch);
//        batchInsert(SETTLEMENT_SQL, settlementBatch);
//        batchInsert(CREDIT_HISTORY_SQL, creditHistoryBatch);
//        batchInsert(REVIEW_SQL, reviewBatch);
//
//        // UserRatingStat 삽입
//        List<Object[]> ratingStatBatch = receiverRatingStat.entrySet().stream()
//                .map(e -> new Object[]{e.getKey(), e.getValue()[0], e.getValue()[1]})
//                .collect(Collectors.toList());
//        batchInsert(USER_RATING_STAT_SQL, ratingStatBatch);
//
//        // UserSkillRatingStat 삽입 (v: [sum, cnt, s1, s2, s3, s4, s5])
//        List<Object[]> skillStatBatch = skillRatingStat.entrySet().stream()
//                .map(e -> {
//                    int[] v = e.getValue();
//                    return new Object[]{e.getKey(), v[0], v[1], v[2], v[3], v[4], v[5], v[6]};
//                })
//                .collect(Collectors.toList());
//        batchInsert(USER_SKILL_RATING_STAT_SQL, skillStatBatch);
//
//        log.info("[Seeder] COMPLETED 거래 {}건 | Review {}건 | RatingStat {}건 | SkillRatingStat {}건",
//                globalIdx, reviewBatch.size(), ratingStatBatch.size(), skillStatBatch.size());
//    }
//
//    /**
//     * 기타 거래 3,000건
//     * PENDING  1,500건: 미래 날짜, CreditHistory 없음
//     * REJECTED 1,000건: CreditHistory USE + 환불 ADD
//     * CANCELED   500건: CreditHistory USE + 환불 ADD
//     */
//    private void createOtherExchanges(List<Long> generalUserIds,
//                                      List<Long> counterpartUserIds,
//                                      List<Long> cpSkillIds,
//                                      List<String> cpSkillNames,
//                                      List<Integer> cpSkillDurations) {
//
//        // == PENDING 1,500건 ==
//        List<Object[]> pendingBatch = new ArrayList<>(PENDING_COUNT);
//        for (int i = 0; i < PENDING_COUNT; i++) {
//            int cpIdx = i % COUNTERPART_COUNT;
//            Long requesterId = generalUserIds.get(ACCEPTED_COUNT + i);
//            Long receiverId = counterpartUserIds.get(cpIdx);
//            Long skillId = cpSkillIds.get(cpIdx);
//            String skillName = cpSkillNames.get(cpIdx);
//            int duration = cpSkillDurations.get(cpIdx);
//            int creditPrice = duration / 30;
//
//            LocalDate date = PENDING_DATE_BASE.plusDays(i % 270);
//            pendingBatch.add(new Object[]{
//                    requesterId, receiverId, skillId,
//                    skillName, duration,
//                    java.sql.Date.valueOf(date),
//                    java.sql.Time.valueOf(EXCHANGE_START),
//                    java.sql.Time.valueOf(EXCHANGE_END),
//                    Timestamp.valueOf(date.atStartOfDay()),
//                    creditPrice, "PENDING"
//            });
//        }
//        batchInsert(EXCHANGE_SQL, pendingBatch);
//        log.info("[Seeder] PENDING 거래 {}건", PENDING_COUNT);
//
//        // == REJECTED 1,000건 ==
//        long baseIdRej = queryMaxExchangeId();
//        List<Object[]> rejectedBatch = new ArrayList<>(REJECTED_COUNT);
//        List<Object[]> rejCreditBatch = new ArrayList<>(REJECTED_COUNT * 2);
//
//        for (int i = 0; i < REJECTED_COUNT; i++) {
//            int cpIdx = i % COUNTERPART_COUNT;
//            Long requesterId = generalUserIds.get(ACCEPTED_COUNT + PENDING_COUNT + i);
//            Long receiverId = counterpartUserIds.get(cpIdx);
//            Long skillId = cpSkillIds.get(cpIdx);
//            String skillName = cpSkillNames.get(cpIdx);
//            int duration = cpSkillDurations.get(cpIdx);
//            int creditPrice = duration / 30;
//
//            LocalDate date = PAST_DATE_BASE.plusDays(i % 200);
//            rejectedBatch.add(new Object[]{
//                    requesterId, receiverId, skillId,
//                    skillName, duration,
//                    java.sql.Date.valueOf(date),
//                    java.sql.Time.valueOf(EXCHANGE_START),
//                    java.sql.Time.valueOf(EXCHANGE_END),
//                    Timestamp.valueOf(date.atStartOfDay()),
//                    creditPrice, "REJECTED"
//            });
//
//            long exchangeId = baseIdRej + i + 1;
//            // USE (요청 시 차감)
//            rejCreditBatch.add(new Object[]{
//                    requesterId, receiverId, exchangeId,
//                    "EXCHANGE_REQUEST", "USE", creditPrice,
//                    INITIAL_CREDIT - creditPrice, skillName
//            });
//            // ADD (거절 시 환불)
//            rejCreditBatch.add(new Object[]{
//                    requesterId, receiverId, exchangeId,
//                    "EXCHANGE_REJECTED", "ADD", creditPrice,
//                    INITIAL_CREDIT, skillName
//            });
//        }
//        batchInsert(EXCHANGE_SQL, rejectedBatch);
//        batchInsert(CREDIT_HISTORY_SQL, rejCreditBatch);
//        log.info("[Seeder] REJECTED 거래 {}건 | CreditHistory {}건", REJECTED_COUNT, rejCreditBatch.size());
//
//        // == CANCELED 500건 ==
//        long baseIdCan = queryMaxExchangeId();
//        List<Object[]> canceledBatch = new ArrayList<>(CANCELED_COUNT);
//        List<Object[]> canCreditBatch = new ArrayList<>(CANCELED_COUNT * 2);
//
//        for (int i = 0; i < CANCELED_COUNT; i++) {
//            int cpIdx = i % COUNTERPART_COUNT;
//            Long requesterId = generalUserIds.get(ACCEPTED_COUNT + PENDING_COUNT + REJECTED_COUNT + i);
//            Long receiverId = counterpartUserIds.get(cpIdx);
//            Long skillId = cpSkillIds.get(cpIdx);
//            String skillName = cpSkillNames.get(cpIdx);
//            int duration = cpSkillDurations.get(cpIdx);
//            int creditPrice = duration / 30;
//
//            LocalDate date = PAST_DATE_BASE.plusDays(i % 200);
//            canceledBatch.add(new Object[]{
//                    requesterId, receiverId, skillId,
//                    skillName, duration,
//                    java.sql.Date.valueOf(date),
//                    java.sql.Time.valueOf(EXCHANGE_START),
//                    java.sql.Time.valueOf(EXCHANGE_END),
//                    Timestamp.valueOf(date.atStartOfDay()),
//                    creditPrice, "CANCELED"
//            });
//
//            long exchangeId = baseIdCan + i + 1;
//            // USE (요청 시 차감)
//            canCreditBatch.add(new Object[]{
//                    requesterId, receiverId, exchangeId,
//                    "EXCHANGE_REQUEST", "USE", creditPrice,
//                    INITIAL_CREDIT - creditPrice, skillName
//            });
//            // ADD (취소 시 환불)
//            canCreditBatch.add(new Object[]{
//                    requesterId, receiverId, exchangeId,
//                    "EXCHANGE_CANCELED", "ADD", creditPrice,
//                    INITIAL_CREDIT, skillName
//            });
//        }
//        batchInsert(EXCHANGE_SQL, canceledBatch);
//        batchInsert(CREDIT_HISTORY_SQL, canCreditBatch);
//        log.info("[Seeder] CANCELED 거래 {}건 | CreditHistory {}건", CANCELED_COUNT, canCreditBatch.size());
//    }
//
//
//    /**
//     * 평점 분포: 5점 40%, 4점 35%, 3점 15%, 2점 5%, 1점 5%
//     */
//    private int resolveRating(int idx) {
//        int mod = idx % 20;
//        if (mod < 8) return 5; // 40%
//        if (mod < 15) return 4; // 35%
//        if (mod < 18) return 3; // 15%
//        if (mod < 19) return 2; // 5%
//        return 1;               // 5%
//    }
//
//    /**
//     * skill_exchange 테이블의 현재 최대 ID 조회.
//     * JdbcTemplate 배치 삽입 후 시퀀셜 ID 계산에 사용.
//     */
//    private long queryMaxExchangeId() {
//        Long max = jdbcTemplate.queryForObject(
//                "SELECT COALESCE(MAX(skill_exchange_id), 0) FROM skill_exchange", Long.class);
//        return max != null ? max : 0L;
//    }
//
//    /**
//     * JdbcTemplate batch insert (BATCH_SIZE 단위로 분할)
//     */
//    private void batchInsert(String sql, List<Object[]> batch) {
//        if (batch.isEmpty()) return;
//        for (int i = 0; i < batch.size(); i += BATCH_SIZE) {
//            List<Object[]> chunk = batch.subList(i, Math.min(i + BATCH_SIZE, batch.size()));
//            jdbcTemplate.batchUpdate(sql, chunk);
//        }
//    }
//}
