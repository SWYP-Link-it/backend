package org.swyp.linkit.domain.credit.service;

import org.swyp.linkit.domain.credit.dto.RewardHistoryDto;
import org.swyp.linkit.domain.credit.dto.response.CreditHistoryResponseDto;
import org.swyp.linkit.domain.credit.entity.CreditHistory;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.entity.SupplyType;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.user.entity.User;

public interface CreditHistoryService {

    // 리워드 크레딧 내역 생성 -> 외부 도메인
    RewardHistoryDto createRewardHistory(User user, int amount, int balanceAfter, HistoryType historyType);
    // 스킬 교환 크레딧 내역 생성 -> 내부 도메인
    CreditHistory createExchangeHistory(User user, User targetUser, SkillExchange skillExchange,
                                        SupplyType supplyType, int amount, int balanceAfter,
                                        HistoryType historyType);
    CreditHistoryResponseDto getUserCreditHistories(Long userId, SupplyType supplyType, Long cursorId, int size);
}

