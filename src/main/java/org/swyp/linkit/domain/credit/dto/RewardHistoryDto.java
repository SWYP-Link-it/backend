package org.swyp.linkit.domain.credit.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.credit.entity.CreditHistory;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.entity.SupplyType;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class RewardHistoryDto {

    private Long historyId;
    private Long userId;
    private String contentName;
    private SupplyType supplyType;
    private int changeAmount;
    private int balanceAfter;
    private HistoryType historyType;

    public static RewardHistoryDto from(CreditHistory creditHistory) {
        return RewardHistoryDto.builder()
                .historyId(creditHistory.getId())
                .userId(creditHistory.getUser().getId())
                .contentName(creditHistory.getContentName())
                .supplyType(creditHistory.getSupplyType())
                .changeAmount(creditHistory.getChangeAmount())
                .balanceAfter(creditHistory.getBalanceAfter())
                .historyType(creditHistory.getHistoryType()).build();
    }

}
