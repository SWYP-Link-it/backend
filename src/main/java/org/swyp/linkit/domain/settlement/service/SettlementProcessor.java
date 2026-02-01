package org.swyp.linkit.domain.settlement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.credit.service.CreditService;
import org.swyp.linkit.domain.settlement.entity.Settlement;
import org.swyp.linkit.domain.user.entity.UserProfile;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementProcessor {

    private final CreditService creditService;

    // 새로운 트랜잭션 적용
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleSettlement(Settlement settlement){
        log.debug("정산 처리 시작. settlementId: {}", settlement.getId());

        // pending -> completed 처리
        // -> InvalidSettlementStatusException
        settlement.complete();

        // 가르친 횟수 증가
        UserProfile receiverProfile = settlement.getSkillExchange().getReceiverSkill().getUserProfile();
        receiverProfile.incrementTimesTaught();

        // 크레딧 정산
        creditService.settleCredit(settlement);
        log.debug("정산 처리 완료. settlementId: {}", settlement.getId());
    }
}
