package org.swyp.linkit.domain.credit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.credit.dto.CreditBalanceUpdateDto;
import org.swyp.linkit.domain.credit.dto.CreditDto;
import org.swyp.linkit.domain.credit.dto.CreditWithUserDetailsDto;
import org.swyp.linkit.domain.credit.entity.Credit;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.repository.CreditRepository;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.global.error.exception.NotFoundCreditException;

@Service
@RequiredArgsConstructor
@Slf4j
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
        log.debug("크레딧 생성. userId= {}", user.getId());
        return CreditDto.from(credit);
    }

    /**
     * 회원가입(닉네임 설정까지) 크레딧 지급
     */
    @Transactional
    @Override
    public CreditDto rewardCreditOnSignupSetup(User user) {
        log.info("회원가입 리워드 지급. userId= {}", user.getId());
        return applyReward(user, SIGNUP_REWARD, HistoryType.SIGNUP_REWARD);
    }

    /**
     * 프로필 설정 완료 크레딧 지급
     */
    @Transactional
    @Override
    public CreditDto rewardCreditOnProfileSetup(User user) {
        log.info("프로필 설정 리워드 지급. userId= {}", user.getId());
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
        log.debug("크레딧 잔액 조회. userId= {}, balance= {}", userId, credit.getBalance());
        return CreditDto.from(credit);
    }

    /**
     *  크레딧 사용
     */
    @Transactional
    @Override
    public CreditBalanceUpdateDto useCredit(Long userId, int amount) {
        // 1. credit 조회
        Credit credit = getCreditByUserId(userId);
        // 2. 크레딧 차감 -> NotEnoughCreditException
        credit.useCredit(amount);
        log.info("크레딧 차감 완료. userId= {}, amount= {}, balance= {}", userId, amount, credit.getBalance());
        return CreditBalanceUpdateDto.of(credit, amount);
    }

    /**
     *  크레딧 잔액 및 유저 정보 조회
     */
    @Transactional(readOnly = true)
    @Override
    public CreditWithUserDetailsDto getCreditBalanceWithUserDetails(Long userId) {
        // 1. credit 조회
        Credit credit = getCreditByUserId(userId);
        // 2. user 객체 접근 -> 조회 쿼리 1번 추가 발생
        User user = credit.getUser();
        log.debug("크레딧 잔액 및 유저 정보 조회. userId= {}, balance= {}", userId, credit.getBalance());
        return CreditWithUserDetailsDto.from(credit, user);
    }

    /**
     *  Credit 조회 및 지급 + CreditHistory 생성 및 save 처리
     */
    private CreditDto applyReward(User user, int amount, HistoryType type) {
        // 1. credit 조회
        Credit credit = getCreditByUserId(user.getId());
        // 2. credit 지급
        credit.addCredit(amount);
        // 3. creditHistory 생성
        historyService.createRewardHistory(user, amount, credit.getBalance(), type);
        return CreditDto.from(credit);
    }

    /**
     *  Credit 조회
     */
    private Credit getCreditByUserId(Long userId){
        return creditRepository.findByUserId(userId)
                .orElseThrow(NotFoundCreditException::new);
    }
}