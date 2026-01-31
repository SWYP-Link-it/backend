package org.swyp.linkit.domain.exchange.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.swyp.linkit.domain.exchange.service.SkillExchangeService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SkillExchangeExpireScheduler {

    private final SkillExchangeService exchangeService;

    /**
     *  매일 12시 00분에 거래 날짜 전날까지 수락되지 않은 요청 거절 처리(expired)
     */
    @Scheduled(cron = "${schedules.cancel-cron}", zone = "Asia/Seoul")
    public void runExpireRequests(){
        log.info("== 수락 되지 않은 거래 자동 만료(거절) 시작 ==");
        int updatedCount = exchangeService.expirePendingRequests();
        log.info("수락 되지 않은 거래 자동 만료(거절) 처리 완료. 건수: {}", updatedCount);
        log.info("== 수락 되지 않은 거래 자동 만료(거절) 종료 ==");
    }
}
