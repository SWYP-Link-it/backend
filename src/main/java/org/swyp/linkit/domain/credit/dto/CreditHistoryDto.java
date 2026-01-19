package org.swyp.linkit.domain.credit.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.credit.entity.CreditHistory;
import org.swyp.linkit.domain.credit.entity.HistoryType;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CreditHistoryDto {

    private Long id;
    private Long userId;
    private int changeAmount;
    private int balanceAfter;
    private HistoryType historyType;
    // 크레딧 변동이 스킬 교환일 경우만 유효
    private Long skillExchangeId;
    // 크레딧 변동이 스킬 교환일 경우만 유효
    private Long targetUserId;

    public static CreditHistoryDto from(CreditHistory creditHistory) {
        return CreditHistoryDto.builder()
                .id(creditHistory.getId())
                .userId(creditHistory.getUser().getId())
                .changeAmount(creditHistory.getChangeAmount())
                .balanceAfter(creditHistory.getBalanceAfter())
                .skillExchangeId(creditHistory.getSkillExchangeId())
                .targetUserId(creditHistory.getTargetUserId())
                .historyType(creditHistory.getHistoryType()).build();
    }

}
