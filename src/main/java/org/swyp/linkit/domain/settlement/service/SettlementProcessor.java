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

    /**
     * 스킬 거래 완료 처리 (skillExchange ACCEPTED -> COMPLETED)
     */
    // 새로운 트랜잭션 적용
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeSingleSkillExchange(Long settlementId){
        log.info("스킬 거래 완료 처리 시작. settlementId: {}", settlementId);
        // 1. settlement 조회 (SkillExchange, Receiver, Requester, userProfile Fetch Join)
        Settlement settlement = settlementRepository.findByIdForSettlement(settlementId)
                .orElseThrow(SettlementNotFoundException::new);

        // 스킬 거래 ACCEPTED -> SETTLED 처리
        SkillExchange skillExchange = settlement.getSkillExchange();
        skillExchange.complete();

        log.info("스킬 거래 완료 처리 완료. settlementId: {}, skillExchangeId: {}",
                settlementId, skillExchange.getId());    }

    /**
     * 정산 처리 (settlement PENDING -> COMPLETED, 멘토 크레딧 지급)
     */
    // 새로운 트랜잭션 적용
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleSettlement(Long settlementId){
        log.info("정산 처리 시작. settlementId: {}", settlementId);
        // 1. settlement 조회 (SkillExchange, Receiver, Requester, userProfile Fetch Join)
        Settlement settlement = settlementRepository.findByIdForSettlement(settlementId)
                .orElseThrow(SettlementNotFoundException::new);

        try{
            // 정산 PENDING -> COMPLETED 처리->InvalidSettlementStatusException
            settlement.complete();

            // 가르친 횟수 증가
            // 정산 과정에서 회원 탈퇴로 Receiver의 Profile이 존재하지 않을 시 가르친 횟수 증가는 생략
            // todo 추후 회원 탈퇴 로직 확실해질때 진행
            UserProfile receiverProfile = settlement.getReceiver().getUserProfile();
            if (receiverProfile != null) {
                receiverProfile.incrementTimesTaught();
            } else {
                log.warn("정산 대상자의 프로필이 존재하지 않아 가르친 횟수 증가를 생략합니다. " +
                        "settlementId: {}, userId: {}", settlementId, settlement.getReceiver().getId());
            }

            // 크레딧 정산
            creditService.settleCredit(settlement);

            log.info("정산 처리 완료. settlementId: {}", settlement.getId());

        } catch (Exception e) {
            log.error("정산 처리 중 오류 발생. settlementId: {}, error: {}",
                    settlementId, e.getMessage(), e);

            // 외부에서 실패 카운트를 위해 예외 전파
            throw e;
        }
    }

}
