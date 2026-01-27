package org.swyp.linkit.domain.credit.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.swyp.linkit.domain.credit.entity.Credit;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CreditBalanceUpdateDto {

    private Long id;
    private int amount;
    private int afterBalance;

    public static CreditBalanceUpdateDto of(Credit credit, int amount){
        return new CreditBalanceUpdateDto(credit.getId(), amount, credit.getBalance());
    }
}
