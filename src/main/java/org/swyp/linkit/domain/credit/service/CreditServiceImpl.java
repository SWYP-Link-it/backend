package org.swyp.linkit.domain.credit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.credit.dto.CreditDto;
import org.swyp.linkit.domain.credit.dto.CreditWithUserDetailsDto;
import org.swyp.linkit.domain.credit.entity.Credit;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.entity.SupplyType;
import org.swyp.linkit.domain.credit.repository.CreditRepository;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.global.error.exception.NotEnoughCreditException;
import org.swyp.linkit.global.error.exception.NotFoundCreditException;

import static org.swyp.linkit.domain.credit.entity.Credit.PROFILE_REWARD;
import static org.swyp.linkit.domain.credit.entity.Credit.SIGNUP_REWARD;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditServiceImpl implements CreditService{

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
        // 1. credit 조회 -> NotFoundCreditException
        Credit credit = getCreditByUserId(userId);
        log.debug("크레딧 잔액 조회. userId= {}, balance= {}", userId, credit.getBalance());
        return CreditDto.from(credit);
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
     * 크레딧 사용 가능 여부 검증
     * 잔액이 요청 금액보다 부족한 경우
     */
    @Transactional(readOnly = true)
    @Override
    public void validateAvailableBalance(Long userId, int amount) {
        // 1. credit 조회 -> NotFoundCreditException
        Credit credit = getCreditByUserId(userId);
        // 2. 잔액이 요청 금액보다 부족 여부 검증 -> NotEnoughCreditException
        if(credit.getBalance() < amount){
            throw new NotEnoughCreditException();
        }
    }

    /**
     *  크레딧 사용 - 스킬 교환 요청
     *  (requester에게 credit 감소 및 creditHistory 생성)
     */
    @Transactional
    @Override
    public void useCreditForExchangeRequest(SkillExchange skillExchange) {
        User requester = skillExchange.getRequester();
        int amount = skillExchange.getCreditPrice();
        // 1. credit 조회 -> NotFoundCreditException
        Credit credit = getCreditByUserId(requester.getId());
        // 2. credit 차감 -> NotEnoughCreditException
        credit.useCredit(amount);
        // 3. creditHistory 생성
        historyService.createExchangeHistory(
                requester,
                skillExchange.getReceiver(),
                skillExchange,
                SupplyType.USE,
                amount,
                credit.getBalance(),
                HistoryType.EXCHANGE_REQUEST
        );

        log.info("크레딧 차감 완료. userId= {}, amount= {}, balance= {}", requester.getId(), amount, credit.getBalance());
    }

    /**
     *  크레딧 수급 - 스킬 교환 취소 및 거절
     *  (requester에게 credit 지급 및 creditHistory 생성)
     */
    @Transactional
    @Override
    public void refundCreditForExchange(SkillExchange skillExchange, SupplyType supplyType,
                                        HistoryType historyType) {
        User requester = skillExchange.getRequester();
        int amount = skillExchange.getCreditPrice();
        // 1. credit 조회 -> NotFoundCreditException
        Credit credit = getCreditByUserId(requester.getId());

        // 2. credit 지급 -> InvalidCreditAmountException
        credit.addCredit(amount);

        // 3. creditHistory 생성
        historyService.createExchangeHistory(
                requester,
                skillExchange.getReceiver(),
                skillExchange,
                supplyType,
                amount,
                credit.getBalance(),
                historyType
        );
    }
    //todo 여기까지 테스트 완료

    // 정산 -> receiver 에게 크레딧 지급 및 creditHistory 생성


    // == private Method ==

    // Credit 조회 및 지급 + CreditHistory 생성 및 save 처리
    private CreditDto applyReward(User user, int amount, HistoryType type) {
        // 1. credit 조회 -> NotFoundCreditException
        Credit credit = getCreditByUserId(user.getId());
        // 2. credit 지급 -> InvalidCreditAmountException
        credit.addCredit(amount);
        // 3. creditHistory 생성
        historyService.createRewardHistory(user, amount, credit.getBalance(), type);
        return CreditDto.from(credit);
    }

    // Credit 조회 -> NotFoundCreditException
    private Credit getCreditByUserId(Long userId){
        return creditRepository.findByUserId(userId)
                .orElseThrow(NotFoundCreditException::new);
    }
}