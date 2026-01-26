package org.swyp.linkit.domain.exchange.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "멘토의 날짜 별 거래 가능 시간 조회 응답")
public class AvailableSlotsResponseDto {

    // ISO 8601 형식 -> "2026-01-25"
    @Schema(description = "요청한 날짜", example = "2026-01-25")
    private String date;

    @Schema(description = "30분 단위의 시간대")
    private List<SlotDto> slots;

    public static AvailableSlotsResponseDto of(String date, List<SlotDto> slots) {
        return new AvailableSlotsResponseDto(date, slots);
    }
}
