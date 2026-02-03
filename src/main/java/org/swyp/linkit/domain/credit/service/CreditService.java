package org.swyp.linkit.domain.credit.service;

import org.swyp.linkit.domain.credit.dto.CreditDto;
import org.swyp.linkit.domain.credit.dto.CreditWithUserDetailsDto;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.settlement.entity.Settlement;
import org.swyp.linkit.domain.user.entity.User;

public interface CreditService {

    CreditDto createCredit(User user);
    CreditDto rewardCreditOnSignupSetup(User user);
    CreditDto rewardCreditOnProfileSetup(User user);
    CreditDto getCreditBalance(Long userId);
    void validateAvailableBalance(Long userId, int amount);
    void useCreditForExchangeRequest(SkillExchange skillExchange);
    void refundCreditForExchange(SkillExchange skillExchange, HistoryType historyType);
    void settleCredit(Settlement settlement);
    CreditWithUserDetailsDto getCreditBalanceWithUserDetails(Long userId);
}