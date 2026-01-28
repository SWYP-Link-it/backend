package org.swyp.linkit.domain.exchange.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "스킬 거래 요청 응답")
public class SkillExchangeResponseDto {

    @Schema(description = "스킬 거래 식별자(ID)", example = "1")
    private Long skillExchangeId;

    @Schema(
            description = "스킬 거래 상태(PENDING, ACCEPTED, REJECTED, PROCESSING, COMPLETED, EXPIRED, CANCELED, SETTLED)",
            example = "PENDING"
    )
    private String exchangeStatus;

    public static SkillExchangeResponseDto from(SkillExchange exchange){
        return new SkillExchangeResponseDto(exchange.getId(), exchange.getExchangeStatus().getDescription());
    }
}

