package org.swyp.linkit.domain.settlement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.credit.service.CreditService;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.settlement.entity.Settlement;
import org.swyp.linkit.domain.settlement.repository.SettlementRepository;
import org.swyp.linkit.domain.user.entity.UserProfile;
import org.swyp.linkit.global.error.exception.SettlementNotFoundException;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementProcessor {

    private final CreditService creditService;
    private final SettlementRepository settlementRepository;

    // 새로운 트랜잭션 적용
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleSettlement(Long settlementId){
        log.info("정산 처리 시작. settlementId: {}", settlementId);
        // 1. settlement 조회 (SkillExchange, Receiver, Requester, userProfile Fetch Join)
        Settlement settlement = settlementRepository.findByIdForSettlement(settlementId)
                .orElseThrow(SettlementNotFoundException::new);

        // 정산 PENDING -> COMPLETED 처리
        // -> InvalidSettlementStatusException
        settlement.complete();

        // 스킬 거래 ACCEPTED -> SETTLED 처리
        SkillExchange skillExchange = settlement.getSkillExchange();
        skillExchange.settled();

        // 가르친 횟수 증가
        // 정산 과정에서
        UserProfile receiverProfile = settlement.getReceiver().getUserProfile();
        receiverProfile.incrementTimesTaught();

        // 크레딧 정산
        creditService.settleCredit(settlement);
        log.info("정산 처리 완료. settlementId: {}", settlement.getId());
    }
}
