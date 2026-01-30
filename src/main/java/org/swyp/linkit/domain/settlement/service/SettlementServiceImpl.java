package org.swyp.linkit.domain.settlement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.settlement.entitiy.Settlement;
import org.swyp.linkit.domain.settlement.repository.SettlementRepository;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.global.error.exception.SettlementNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementServiceImpl implements SettlementService{

    private final SettlementRepository repository;

    /**
     *  Settlement 생성
     */
    @Transactional
    @Override
    public void createSettlement(SkillExchange skillExchange) {
        User receiver = skillExchange.getReceiver();
        repository.save(Settlement.create(skillExchange, receiver));
        log.info("정산 데이터 생성 완료. skillExchangeId= {}, receiverId= {}", skillExchange.getId(), receiver.getId());
    }

    /**
     *  거래 수락 후 취소 시 정산 상태 변경(PENDING -> CANCELED)
     */
    @Transactional
    @Override
    public void cancelSettlement(Long skillExchangeId) {
        // 조회
        Settlement settlement = repository.findBySkillExchangeId(skillExchangeId)
                .orElseThrow(SettlementNotFoundException::new);

        // cancel 처리
        settlement.cancel();
        log.info("정산 취소 완료. settlementId= {}, skillExchangeId= {}", settlement.getId(), skillExchangeId);
    }

    /**
     *  자동 정산 실행 (스케줄러 전용)
     *  종료 시간이 지난 대기 중인 정산 건들을 처리하고 크레딧 지급
     */
    @Transactional
    @Override
    public int processAutoSettlement() {
        return 0;
        // 유저 가르친 횟수
    }


}
