package org.swyp.linkit.domain.credit.service;

import org.swyp.linkit.domain.credit.dto.CreditDto;
import org.swyp.linkit.domain.user.entity.User;

public interface CreditService {

    CreditDto createCredit(User user);
    CreditDto rewardCreditOnSignupSetup(User user);
    CreditDto rewardCreditOnProfileSetup(User user);
    CreditDto getCreditBalance(Long userId);
}