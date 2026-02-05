package org.swyp.linkit.domain.settlement.service;

import org.swyp.linkit.domain.exchange.entity.SkillExchange;

public interface SettlementService {

    /**
     *  거래 수락 시 정산 데이터 생성(PENDING)
     */
    void createSettlement(SkillExchange skillExchange);

    /**
     *  거래 수락 후 취소 시 정산 상태 변경(PENDING -> CANCELED)
     */
    void cancelSettlement(Long skillExchangeId);

    /**
     *  자동 정산 실행 (스케줄러 전용)
     *  종료 시간이 지난 대기 중인 정산 건들을 처리하고 크레딧 지급
     */
    int processAutoSettlement();
}
