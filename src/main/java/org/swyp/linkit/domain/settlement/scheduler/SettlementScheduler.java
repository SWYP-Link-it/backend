package org.swyp.linkit.domain.settlement.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.swyp.linkit.domain.settlement.service.SettlementService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementScheduler {

    private final SettlementService settlementService;

    /**
     *  매일 (N시 00분, N시 00분)에 정산 처리
     */
    @Scheduled(cron = "${schedules.settle-cron}", zone = "Asia/Seoul")
    public void runAutoRequests(){
        log.info("== 정산 처리 시작 ==");
        int updatedCount = settlementService.processAutoSettlement();
        log.info("정산 처리 완료. 건수: {}", updatedCount);
        log.info("== 정산 처리 종료 ==");
    }
}
