package org.swyp.linkit.global.loadtest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.domain.settlement.service.SettlementService;

import java.util.Map;

/**
 * 정산 스케줄러 성능 측정용 컨트롤러 (부하 테스트 전용)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/load-test/settlement")
public class LoadTestSettlementController {

    private final SettlementService settlementService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 종료 시간이 지난 PENDING 정산 전부를 처리하고 크레딧을 지급한다.
     * Response: { "processedCount": 10000, "elapsedMs": 1234 }
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> process() {
        long startMs = System.currentTimeMillis();
        int processedCount = settlementService.processAutoSettlement();
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
     *
     * Response: { "canceledExchanges": N, "creditReset": M }
     */
    @PostMapping("/exchange/reset")
    public ResponseEntity<Map<String, Object>> resetExchangeSlots() {
        // PENDING → CANCELED (booked 슬롯 해제)
        int canceled = jdbcTemplate.update(
                "UPDATE skill_exchange se " +
                "JOIN users u ON se.receiver_id = u.id " +
                "SET se.exchange_status = 'CANCELED' " +
                "WHERE se.exchange_status = 'PENDING' " +
                "AND u.nickname LIKE 'mentor%'");

        // requester 크레딧 잔액 초기화
        int creditReset = jdbcTemplate.update(
                "UPDATE credit c " +
                "JOIN users u ON c.user_id = u.id " +
                "SET c.balance = 200 " +
                "WHERE u.nickname LIKE 'requester%'");

        return ResponseEntity.ok(Map.of(
                "canceledExchanges", canceled,
                "creditReset", creditReset));
    }

    /**
     * process 실행 후 Before/After 재측정을 위해 PENDING 상태로 되돌린다.
     *
     * 초기화 범위:
     *   - Settlement  : COMPLETED → PENDING (settled_at = NULL)
     *   - SkillExchange: COMPLETED → ACCEPTED
     *
     * 주의: 크레딧(EXCHANGE_SETTLED 내역)은 롤백하지 않는다.
     *       재측정 시 receiver 크레딧이 누적되지만, 쿼리 수 측정에는 영향 없음.
     *
     * Response: { "resetSettlements": 10000, "resetExchanges": 10000, "elapsedMs": 456 }
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> reset() {
        long startMs = System.currentTimeMillis();

        // Settlement COMPLETED → PENDING
        // 식별 기준: requester가 시더 general 유저 (nickname = 'user_*')
        int resetSettlements = jdbcTemplate.update(
                "UPDATE settlement s " +
                "JOIN skill_exchange se ON s.skill_exchange_id = se.skill_exchange_id " +
                "JOIN users u ON se.requester_id = u.id " +
                "SET s.status = 'PENDING', s.settled_at = NULL " +
                "WHERE s.status = 'COMPLETED' " +
                "AND u.nickname LIKE 'user\\_%' ESCAPE '\\'");

        // SkillExchange COMPLETED → ACCEPTED (동일 대상)
        int resetExchanges = jdbcTemplate.update(
                "UPDATE skill_exchange se " +
                "JOIN users u ON se.requester_id = u.id " +
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
}
