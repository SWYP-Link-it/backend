package org.swyp.linkit.domain.credit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.credit.dto.CreditHistoryDto;
import org.swyp.linkit.domain.credit.entity.CreditHistory;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.repository.CreditHistoryRepository;
import org.swyp.linkit.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class CreditHistoryServiceImpl implements CreditHistoryService{

    private final CreditHistoryRepository historyRepository;

    @Transactional
    @Override
    public CreditHistoryDto createRewardHistory(User user, int changeAmount,
                                                int balanceAfter, HistoryType historyType) {
        // creditHistory 생성, save
        CreditHistory creditHistory = historyRepository.save
                (CreditHistory.createReward(user, changeAmount, balanceAfter, historyType));

        return CreditHistoryDto.from(creditHistory);
    }
}
