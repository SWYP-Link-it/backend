package org.swyp.linkit.domain.exchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.service.CreditService;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.exchange.repository.SkillExchangeRepository;
import org.swyp.linkit.domain.notification.entity.NotificationType;
import org.swyp.linkit.domain.notification.service.NotificationService;
import org.swyp.linkit.global.error.exception.ExchangeNotFoundException;

@Component
@RequiredArgsConstructor
@Slf4j
public class SkillExchangeExpireProcessor {

    private final SkillExchangeRepository skillExchangeRepository;
    private final CreditService creditService;
    private final NotificationService notificationService;

    // 새로운 트랜잭션 적용
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expireSingleSkillExchange(Long skillExchangeId){
        log.debug("거래 만료 처리 시작. skillExchangeId: {}", skillExchangeId);

        // 1. skillExchange 조회 (Requester, Receiver Fetch Join)
        SkillExchange skillExchange = skillExchangeRepository.findByIdForExpire(skillExchangeId)
                .orElseThrow(ExchangeNotFoundException::new);

        // pending -> expired 변경
        skillExchange.expire();

        // 크레딧 환불 처리
        creditService.refundCreditForExchange(skillExchange, HistoryType.EXCHANGE_EXPIRED);

        // 알림 생성 — requester: 보낸 요청 탭, receiver: 받은 요청 탭
        notificationService.createSystemNotification(
                skillExchange.getRequester().getId(),
                NotificationType.SENT_REQUEST_STATUS_CHANGED, skillExchange.getId());
        notificationService.createSystemNotification(
                skillExchange.getReceiver().getId(),
                NotificationType.RECEIVED_REQUEST_STATUS_CHANGED, skillExchange.getId());
        log.debug("거래 만료 처리 완료. skillExchangeId: {}", skillExchange.getId());
    }
}
