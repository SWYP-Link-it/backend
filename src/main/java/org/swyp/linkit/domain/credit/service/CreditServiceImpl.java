package org.swyp.linkit.domain.credit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.credit.dto.CreditDto;
import org.swyp.linkit.domain.credit.entity.Credit;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.repository.CreditRepository;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.global.error.exception.NotFoundCreditException;

@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService{

    private static final int SIGNUP_REWARD = 2;
    private static final int PROFILE_REWARD = 1;

    private final CreditRepository creditRepository;
    private final CreditHistoryService historyService;

    /**
     * 크레딧 생성
     */
    @Transactional
    @Override
    public CreditDto createCredit(User user) {
        // credit 생성, save
        Credit credit = creditRepository.save(Credit.create(user, 0));
        return CreditDto.from(credit);
    }

    /**
     * 회원가입(닉네임 설정까지) 크레딧 지급
     */
    @Transactional
    @Override
    public CreditDto rewardCreditOnSignupSetup(User user) {
        return applyReward(user, SIGNUP_REWARD, HistoryType.SIGNUP_REWARD);
    }

    /**
     * 프로필 설정 완료 크레딧 지급
     */
    @Transactional
    @Override
    public CreditDto rewardCreditOnProfileSetup(User user) {
        return applyReward(user, PROFILE_REWARD, HistoryType.PROFILE_REWARD);
    }

    /**
     * 크레딧 잔액 조회
     */
    @Transactional(readOnly = true)
    @Override
    public CreditDto getCreditBalance(Long userId) {
        // 1. credit 조회
        Credit credit = getCreditByUserId(userId);
        return CreditDto.from(credit);
    }

    private CreditDto applyReward(User user, int amount, HistoryType type) {
        // 1. credit 조회
        Credit credit = getCreditByUserId(user.getId());
        // 2. credit 지급
        credit.addCredit(amount);
        // 3. creditHistory 생성
        historyService.createRewardHistory(user, amount, credit.getBalance(), type);
        return CreditDto.from(credit);
    }

    private Credit getCreditByUserId(Long userId){
        return creditRepository.findByUserId(userId)
                .orElseThrow(NotFoundCreditException::new);
    }
}