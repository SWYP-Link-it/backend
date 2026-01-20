package org.swyp.linkit.domain.credit.service;

import org.swyp.linkit.domain.credit.dto.CreditHistoryDto;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.user.entity.User;

public interface CreditHistoryService {

    CreditHistoryDto createRewardHistory(User user, int changeAmount, int balanceAfter, HistoryType historyType);
}
