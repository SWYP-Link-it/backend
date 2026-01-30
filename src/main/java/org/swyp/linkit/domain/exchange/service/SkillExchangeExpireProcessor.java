package org.swyp.linkit.domain.exchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.service.CreditService;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;

@Component
@RequiredArgsConstructor
@Slf4j
public class SkillExchangeExpireProcessor {

    private final CreditService creditService;

    // 새로운 트랜잭션 적용
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expireSingleSkillExchange(SkillExchange skillExchange){
        log.debug("거래 만료 처리 시작. skillExchangeId: {}", skillExchange.getId());

        // pending -> expired 변경
        skillExchange.expire();
        // 확인 안함 처리(requester, receiver)
        skillExchange.updateReceiverReadToFalse();
        skillExchange.updateRequesterReadToFalse();

        // 크레딧 환불 처리
        creditService.refundCreditForExchange(skillExchange, HistoryType.EXCHANGE_EXPIRED);
        log.debug("거래 만료 처리 완료. skillExchangeId: {}", skillExchange.getId());
    }
}
