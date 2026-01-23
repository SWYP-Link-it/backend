package org.swyp.linkit.domain.credit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.credit.dto.RewardHistoryDto;
import org.swyp.linkit.domain.credit.dto.response.CreditHistoryResponseDto;
import org.swyp.linkit.domain.credit.entity.CreditHistory;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.entity.SupplyType;
import org.swyp.linkit.domain.credit.repository.CreditHistoryRepository;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class CreditHistoryServiceImpl implements CreditHistoryService {

    private final CreditHistoryRepository historyRepository;

    @Value("${app.default-profile-image}")
    private String defaultProfileImageUrl;

    /**
     *  리워드 크레딧 내역 생성 -> 외부 도메인
     */
    @Transactional
    @Override
    public RewardHistoryDto createRewardHistory(User user, int amount, int balanceAfter, HistoryType historyType) {
        // creditHistory 생성, save
        CreditHistory creditHistory = historyRepository.save(
                CreditHistory.createReward(user, amount, balanceAfter, historyType)
        );

        return RewardHistoryDto.from(creditHistory);
    }

    /**
     *  스킬 교환 크레딧 내역 생성 -> 내부 도메인
     */
    @Transactional
    @Override
    public CreditHistory createExchangeHistory(User user, User targetUser, SkillExchange skillExchange,
                                               SupplyType supplyType, int amount, int balanceAfter,
                                               HistoryType historyType) {
        // creditHistory 생성, save
        return historyRepository.save(
                CreditHistory.createSkillExchange(
                        user,
                        targetUser,
                        skillExchange,
                        supplyType,
                        amount,
                        balanceAfter,
                        historyType)
        );
    }
    /**
     *  크레딧 내역 커서 기반 페이징 조회
     */
    @Transactional(readOnly = true)
    @Override
    public CreditHistoryResponseDto getUserCreditHistories(Long userId, SupplyType supplyType, Long cursorId, int size) {

        // 1. Pageable 객체 생성
        Pageable pageable = PageRequest.of(0, size);
        // 2. 크레딧 내역 커서 기반 페이징 조회
        Slice<CreditHistory> slice = historyRepository
                .findAllByUserIdAndSupplyType(userId, supplyType, cursorId, pageable);
        // 3. 응답 Dto 변환
        return CreditHistoryResponseDto.of(slice, defaultProfileImageUrl);
    }
}
