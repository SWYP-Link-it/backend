package org.swyp.linkit.domain.settlement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.settlement.entity.Settlement;
import org.swyp.linkit.domain.settlement.entity.SettlementStatus;
import org.swyp.linkit.domain.settlement.repository.SettlementRepository;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.global.error.exception.SettlementNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementProcessor settlementProcessor;

    /**
     * Settlement 생성
     */
    @Transactional
    @Override
    public void createSettlement(SkillExchange skillExchange) {
        User receiver = skillExchange.getReceiver();
        settlementRepository.save(Settlement.create(skillExchange, receiver));
        log.info("정산 데이터 생성 완료. skillExchangeId= {}, receiverId= {}", skillExchange.getId(), receiver.getId());
    }

    /**
     * 거래 수락 후 취소 시 정산 상태 변경(PENDING -> CANCELED)
     */
    @Transactional
    @Override
    public void cancelSettlement(Long skillExchangeId) {
        // 조회
        Settlement settlement = settlementRepository.findBySkillExchangeId(skillExchangeId)
                .orElseThrow(SettlementNotFoundException::new);

        // cancel 처리 -> InvalidSettlementStatusException
        settlement.cancel();
        log.info("정산 취소 완료. settlementId= {}, skillExchangeId= {}", settlement.getId(), skillExchangeId);
    }

    /**
     * 자동 정산 실행 (스케줄러 전용)
     * 종료 시간이 지난 대기 중인 정산 건들을 처리하고 크레딧 지급
     */
    @Transactional(readOnly = true)
    @Override
    public int processAutoSettlement() {
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        // 1. 정산 대상인 Settlement 조회
        List<Long> settleTargetIds = settlementRepository.findSettleTargets(SettlementStatus.PENDING, today, nowTime);
        if (settleTargetIds.isEmpty()) {
            log.info("정산 처리 대상 없음");
            return 0;
        }
        log.info("정산 처리 대상 {}건.", settleTargetIds.size());

        // 2. 각 정산 독립적인 트랜잭션으로 처리
        int successCount = 0;
        int failCount = 0;

        for (Long settlementId : settleTargetIds) {
            try {
                // 2.1. 스킬 거래 완료 처리 (ACCEPTED -> COMPLETED)
                settlementProcessor.completeSingleSkillExchange(settlementId);

                // 2.2. 정산 처리 (크레딧 지급, PENDING -> COMPLETED)
                settlementProcessor.processSingleSettlement(settlementId);

                successCount++;

            } catch (Exception e) {
                failCount++;
                log.error("정산 처리 실패. settlementId: {}, errorMessage: {}", settlementId, e.getMessage());
            }
        }

        log.info("정산 처리 결과 - 성공: {}건, 실패: {}건", successCount, failCount);
        return successCount;
    }
}
