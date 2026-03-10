package org.swyp.linkit.global.loadtest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.domain.settlement.entity.SettlementStatus;
import org.swyp.linkit.domain.settlement.repository.SettlementRepository;
import org.swyp.linkit.domain.settlement.service.SettlementProcessor;
import org.swyp.linkit.domain.settlement.service.SettlementService;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 정산 스케줄러 성능 측정용 컨트롤러 (부하 테스트 전용)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/load-test/settlement")
public class LoadTestSettlementController {

    private final SettlementService settlementService;
    private final SettlementProcessor settlementProcessor;
    private final SettlementRepository settlementRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final int SEED_MENTOR_COUNT = 20;
    private static final int SEED_REQUESTER_COUNT = 2000;
    private static final int BATCH_SIZE = 1000;

    private static final String EXCHANGE_SQL =
            "INSERT INTO skill_exchange " +
                    "(requester_id, receiver_id, receiver_skill_id, skill_name, exchange_duration, " +
                    "scheduled_date, start_time, end_time, request_dead_line, credit_price, " +
                    "exchange_status, is_requester_read, is_receiver_read, message, created_at, modified_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false, false, null, NOW(), NOW())";

    private static final String SETTLEMENT_SQL =
            "INSERT INTO settlement " +
                    "(skill_exchange_id, receiver_id, amount, status, settled_at, created_at, modified_at) " +
                    "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";

    /**
     * 종료 시간이 지난 PENDING 정산을 처리하고 크레딧을 지급한다.
     * @param limit 처리할 최대 건수 (0 = 전체 처리)
     * Response: { "processedCount": N, "elapsedMs": 1234 }
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> process(
            @RequestParam(defaultValue = "0") int limit) {
        long startMs = System.currentTimeMillis();
        int processedCount;

        if (limit <= 0) {
            processedCount = settlementService.processAutoSettlement();
        } else {
            List<Long> targetIds = settlementRepository.findSettleTargets(
                    SettlementStatus.PENDING, LocalDate.now(), LocalTime.now());

            if (targetIds.size() > limit) {
                targetIds = targetIds.subList(0, limit);
            }

            processedCount = 0;
            for (Long id : targetIds) {
                try {
                    settlementProcessor.completeSingleSkillExchange(id);
                    settlementProcessor.processSingleSettlement(id);
                    processedCount++;
                } catch (Exception e) {
                    log.error("정산 처리 실패. settlementId={}, error={}", id, e.getMessage());
                }
            }
        }

        long elapsedMs = System.currentTimeMillis() - startMs;

        return ResponseEntity.ok(Map.of(
                "processedCount", processedCount,
                "elapsedMs", elapsedMs
        ));
    }

    /**
     * 시나리오2 부하 테스트 전 슬롯 초기화.
     * mentor* 닉네임 유저가 receiver인 PENDING 거래를 CANCELED로 변경 → 슬롯 해제.
     * requester* 닉네임 유저의 크레딧 잔액을 200으로 초기화 → 크레딧 고갈 방지.
     * <p>
     * Response: { "canceledExchanges": N, "creditReset": M }
     */
    @PostMapping("/exchange/reset")
    public ResponseEntity<Map<String, Object>> resetExchangeSlots() {
        int canceled = jdbcTemplate.update(
                "UPDATE skill_exchange se " +
                        "JOIN users u ON se.receiver_id = u.user_id " +
                        "SET se.exchange_status = 'CANCELED' " +
                        "WHERE se.exchange_status = 'PENDING' " +
                        "AND u.nickname LIKE 'mentor%'");

        int creditReset = jdbcTemplate.update(
                "UPDATE credit c " +
                        "JOIN users u ON c.user_id = u.user_id " +
                        "SET c.balance = 200 " +
                        "WHERE u.nickname LIKE 'requester%'");

        return ResponseEntity.ok(Map.of(
                "canceledExchanges", canceled,
                "creditReset", creditReset));
    }

    /**
     * process 실행 후 Before/After 재측정을 위해 PENDING 상태로 되돌린다.
     * 대상: requester가 시더 general 유저 (nickname = 'user_*')
     * <p>
     * Response: { "resetSettlements": 10000, "resetExchanges": 10000, "elapsedMs": 456 }
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> reset() {
        long startMs = System.currentTimeMillis();

        int resetSettlements = jdbcTemplate.update(
                "UPDATE settlement s " +
                        "JOIN skill_exchange se ON s.skill_exchange_id = se.skill_exchange_id " +
                        "JOIN users u ON se.requester_id = u.user_id " +
                        "SET s.status = 'PENDING', s.settled_at = NULL " +
                        "WHERE s.status = 'COMPLETED' " +
                        "AND u.nickname LIKE 'user\\_%' ESCAPE '\\'");

        int resetExchanges = jdbcTemplate.update(
                "UPDATE skill_exchange se " +
                        "JOIN users u ON se.requester_id = u.user_id " +
                        "SET se.exchange_status = 'ACCEPTED' " +
                        "WHERE se.exchange_status = 'COMPLETED' " +
                        "AND u.nickname LIKE 'user\\_%' ESCAPE '\\'");

        long elapsedMs = System.currentTimeMillis() - startMs;

        return ResponseEntity.ok(Map.of(
                "resetSettlements", resetSettlements,
                "resetExchanges", resetExchanges,
                "elapsedMs", elapsedMs
        ));
    }

    // =========================================================
    // 정산 성능 테스트 전용 엔드포인트
    // =========================================================

    /**
     * 정산 전용 테스트 데이터 2,000건 생성.
     * settle_mentor_1~20  : receiver (UserProfile + Credit 보유)
     * settle_requester_1~2000 : requester (Credit 보유)
     * SkillExchange ACCEPTED (어제 날짜) + Settlement PENDING
     * <p>
     * 이미 생성된 데이터가 있으면 skip.
     * Response: { "seededSettlements": 2000, "elapsedMs": N }
     */
    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seed() {
        Integer existingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE nickname LIKE 'settle\\_requester\\_%' ESCAPE '\\'",
                Integer.class);
        if (existingCount != null && existingCount > 0) {
            return ResponseEntity.ok(Map.of(
                    "message", "already seeded",
                    "seededSettlements", existingCount));
        }

        long startMs = System.currentTimeMillis();

        // user_skill에서 skill_category_id 조회 (FK이므로 유효한 값 보장)
        Long skillCategoryId = jdbcTemplate.queryForObject(
                "SELECT skill_category_id FROM user_skill LIMIT 1", Long.class);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(10, 30);

        // settle_mentor 20명 INSERT
        List<Object[]> mentorUserBatch = new ArrayList<>();
        for (int i = 1; i <= SEED_MENTOR_COUNT; i++) {
            mentorUserBatch.add(new Object[]{
                    "KAKAO", "settle_mentor_oauth_" + i,
                    "settle_mentor_" + i + "@test.com",
                    "정산멘토" + i, "settle_mentor_" + i, "ACTIVE"
            });
        }
        batchInsert(
                "INSERT INTO users (oauth_provider, oauth_id, email, name, nickname, user_status, created_at, modified_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())",
                mentorUserBatch);

        // mentor ID 조회
        List<Long> mentorIds = jdbcTemplate.queryForList(
                "SELECT user_id FROM users WHERE nickname LIKE 'settle\\_mentor\\_%' ESCAPE '\\' ORDER BY user_id",
                Long.class);

        // mentor user_profile INSERT
        List<Object[]> mentorProfileBatch = new ArrayList<>();
        for (Long mentorId : mentorIds) {
            mentorProfileBatch.add(new Object[]{mentorId, "정산 테스트 멘토", 0, "BOTH", "SEOUL"});
        }
        batchInsert(
                "INSERT INTO user_profile (user_id, experience_description, times_taught, exchange_type, preferred_region, created_at, modified_at) " +
                        "VALUES (?, ?, ?, ?, ?, NOW(), NOW())",
                mentorProfileBatch);

        // mentor user_profile_id 조회
        List<Long> mentorProfileIds = jdbcTemplate.queryForList(
                "SELECT up.user_profile_id FROM user_profile up " +
                        "JOIN users u ON up.user_id = u.user_id " +
                        "WHERE u.nickname LIKE 'settle\\_mentor\\_%' ESCAPE '\\' ORDER BY up.user_profile_id",
                Long.class);

        // mentor user_skill INSERT (receiver_skill_id FK 충족용)
        List<Object[]> mentorSkillBatch = new ArrayList<>();
        for (int i = 0; i < mentorProfileIds.size(); i++) {
            mentorSkillBatch.add(new Object[]{
                    mentorProfileIds.get(i), skillCategoryId,
                    "settle_skill_" + (i + 1), "정산테스트스킬" + (i + 1),
                    "HIGH", "정산 테스트용 스킬", 30, true
            });
        }
        batchInsert(
                "INSERT INTO user_skill (user_profile_id, skill_category_id, skill_name, skill_title, " +
                        "skill_proficiency, skill_description, exchange_duration, is_visible, created_at, modified_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                mentorSkillBatch);

        // mentor user_skill_id 조회
        List<Long> mentorSkillIds = jdbcTemplate.queryForList(
                "SELECT us.user_skill_id FROM user_skill us " +
                        "JOIN user_profile up ON us.user_profile_id = up.user_profile_id " +
                        "JOIN users u ON up.user_id = u.user_id " +
                        "WHERE u.nickname LIKE 'settle\\_mentor\\_%' ESCAPE '\\' ORDER BY us.user_skill_id",
                Long.class);

        // mentor credit INSERT (balance = 0)
        List<Object[]> mentorCreditBatch = new ArrayList<>();
        for (Long mentorId : mentorIds) {
            mentorCreditBatch.add(new Object[]{mentorId, 0});
        }
        batchInsert(
                "INSERT INTO credit (user_id, balance, created_at, modified_at) VALUES (?, ?, NOW(), NOW())",
                mentorCreditBatch);

        // settle_requester 2000명 INSERT
        List<Object[]> requesterUserBatch = new ArrayList<>();
        for (int i = 1; i <= SEED_REQUESTER_COUNT; i++) {
            requesterUserBatch.add(new Object[]{
                    "KAKAO", "settle_req_oauth_" + i,
                    "settle_requester_" + i + "@test.com",
                    "정산요청자" + i, "settle_requester_" + i, "ACTIVE"
            });
        }
        batchInsert(
                "INSERT INTO users (oauth_provider, oauth_id, email, name, nickname, user_status, created_at, modified_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())",
                requesterUserBatch);

        // requester ID 조회
        List<Long> requesterIds = jdbcTemplate.queryForList(
                "SELECT user_id FROM users WHERE nickname LIKE 'settle\\_requester\\_%' ESCAPE '\\' ORDER BY user_id",
                Long.class);

        // requester credit INSERT (balance = 0, 이미 차감된 상태 시뮬레이션)
        List<Object[]> requesterCreditBatch = new ArrayList<>();
        for (Long requesterId : requesterIds) {
            requesterCreditBatch.add(new Object[]{requesterId, 0});
        }
        batchInsert(
                "INSERT INTO credit (user_id, balance, created_at, modified_at) VALUES (?, ?, NOW(), NOW())",
                requesterCreditBatch);

        // skill_exchange 2000건 INSERT (ACCEPTED, 어제 날짜)
        List<Object[]> exchangeBatch = new ArrayList<>();
        for (int i = 0; i < requesterIds.size(); i++) {
            int mentorIdx = i % SEED_MENTOR_COUNT;
            exchangeBatch.add(new Object[]{
                    requesterIds.get(i),
                    mentorIds.get(mentorIdx),
                    mentorSkillIds.get(mentorIdx),
                    "settle-test-skill-" + (mentorIdx + 1),
                    30,
                    java.sql.Date.valueOf(yesterday),
                    java.sql.Time.valueOf(startTime),
                    java.sql.Time.valueOf(endTime),
                    Timestamp.valueOf(yesterday.atStartOfDay()),
                    1, "ACCEPTED"
            });
        }
        batchInsert(EXCHANGE_SQL, exchangeBatch);

        // 생성된 exchange ID 조회
        List<Long> exchangeIds = jdbcTemplate.queryForList(
                "SELECT se.skill_exchange_id FROM skill_exchange se " +
                        "JOIN users u ON se.requester_id = u.user_id " +
                        "WHERE u.nickname LIKE 'settle\\_requester\\_%' ESCAPE '\\' ORDER BY se.skill_exchange_id",
                Long.class);

        // settlement 2000건 INSERT (PENDING)
        List<Object[]> settlementBatch = new ArrayList<>();
        for (int i = 0; i < exchangeIds.size(); i++) {
            int mentorIdx = i % SEED_MENTOR_COUNT;
            settlementBatch.add(new Object[]{
                    exchangeIds.get(i), mentorIds.get(mentorIdx), 1, "PENDING", null
            });
        }
        batchInsert(SETTLEMENT_SQL, settlementBatch);

        long elapsedMs = System.currentTimeMillis() - startMs;
        log.info("[SeedSettlement] 완료 — mentors={}, requesters={}, settlements={}, elapsedMs={}",
                mentorIds.size(), requesterIds.size(), settlementBatch.size(), elapsedMs);

        return ResponseEntity.ok(Map.of(
                "seededSettlements", settlementBatch.size(),
                "elapsedMs", elapsedMs));
    }

    /**
     * settle_requester_* 대상 정산만 처리 (Before/After 성능 비교용).
     * 기존 더미데이터(user_* 등)와 완전 격리.
     * Response: { "processedCount": 2000, "elapsedMs": N }
     */
    @PostMapping("/seed/process")
    public ResponseEntity<Map<String, Object>> seedProcess() {
        List<Long> targetIds = jdbcTemplate.queryForList(
                "SELECT s.settlement_id FROM settlement s " +
                        "JOIN skill_exchange se ON s.skill_exchange_id = se.skill_exchange_id " +
                        "JOIN users u ON se.requester_id = u.user_id " +
                        "WHERE s.status = 'PENDING' " +
                        "AND u.nickname LIKE 'settle\\_requester\\_%' ESCAPE '\\'",
                Long.class);

        long startMs = System.currentTimeMillis();
        int successCount = 0;

        for (Long id : targetIds) {
            try {
                settlementProcessor.completeSingleSkillExchange(id);
                settlementProcessor.processSingleSettlement(id);
                successCount++;
            } catch (Exception e) {
                log.error("[SeedProcess] 정산 처리 실패. settlementId={}, error={}", id, e.getMessage());
            }
        }

        long elapsedMs = System.currentTimeMillis() - startMs;

        return ResponseEntity.ok(Map.of(
                "processedCount", successCount,
                "elapsedMs", elapsedMs));
    }

    /**
     * settle_requester_* 정산을 PENDING 상태로 롤백 (재측정용).
     * user_* 등 기존 데이터에 영향 없음.
     * Response: { "resetSettlements": N, "resetExchanges": N }
     */
    @PostMapping("/seed/reset")
    public ResponseEntity<Map<String, Object>> seedReset() {
        int resetSettlements = jdbcTemplate.update(
                "UPDATE settlement s " +
                        "JOIN skill_exchange se ON s.skill_exchange_id = se.skill_exchange_id " +
                        "JOIN users u ON se.requester_id = u.user_id " +
                        "SET s.status = 'PENDING', s.settled_at = NULL " +
                        "WHERE s.status = 'COMPLETED' " +
                        "AND u.nickname LIKE 'settle\\_requester\\_%' ESCAPE '\\'");

        int resetExchanges = jdbcTemplate.update(
                "UPDATE skill_exchange se " +
                        "JOIN users u ON se.requester_id = u.user_id " +
                        "SET se.exchange_status = 'ACCEPTED' " +
                        "WHERE se.exchange_status = 'COMPLETED' " +
                        "AND u.nickname LIKE 'settle\\_requester\\_%' ESCAPE '\\'");

        return ResponseEntity.ok(Map.of(
                "resetSettlements", resetSettlements,
                "resetExchanges", resetExchanges));
    }

    // =========================================================
    // 내부 유틸
    // =========================================================

    private void batchInsert(String sql, List<Object[]> batch) {
        if (batch.isEmpty()) return;
        for (int i = 0; i < batch.size(); i += BATCH_SIZE) {
            List<Object[]> chunk = batch.subList(i, Math.min(i + BATCH_SIZE, batch.size()));
            jdbcTemplate.batchUpdate(sql, chunk);
        }
    }
}
