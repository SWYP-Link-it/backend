package org.swyp.linkit.domain.credit.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.credit.entity.Credit;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CreditDto {

    private Long id;
    private Long userId;
    private int amount;

    public static CreditDto from(Credit credit) {
        return CreditDto.builder()
                .id(credit.getId())
                .userId(credit.getUser().getId())
                .amount(credit.getAmount())
                .build();
    }
}
