package org.swyp.linkit.domain.exchange.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "멘토의 월별 거래 가능 날짜 조회 응답")
public class AvailableDatesResponseDto {

    // ISO 8601 형식 -> "2026-01-25"
    @Schema(description = "요청한 월", example = "2026-01")
    private String month;

    @Schema(description = "가능한 날짜")
    private List<String> availableDates;

    public static AvailableDatesResponseDto of(String month, List<String> availableDates){
        return new AvailableDatesResponseDto(month, availableDates);
    }

}
