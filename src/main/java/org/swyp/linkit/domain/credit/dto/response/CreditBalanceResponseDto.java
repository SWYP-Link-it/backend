package org.swyp.linkit.domain.credit.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.swyp.linkit.domain.credit.dto.CreditDto;

@Getter
@AllArgsConstructor
@Schema(description = "크레딧 잔액 조회 응답")
public class CreditBalanceResponseDto {

    @Schema(description = "유저 식별자(ID)", example = "1")
    private Long userId;

    @Schema(description = "현재 보유 크레딧 잔액", example = "5")
    private int balance;

    public static CreditBalanceResponseDto from(CreditDto creditDto){
        return new CreditBalanceResponseDto(creditDto.getUserId(), creditDto.getBalance());
    }
}