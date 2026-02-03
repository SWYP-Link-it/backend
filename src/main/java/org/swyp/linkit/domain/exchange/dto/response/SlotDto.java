package org.swyp.linkit.domain.exchange.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@Schema(description = "멘토의 날짜 별 거래 가능 시간, 신청 가능 여부 조회 응답")
public class SlotDto {

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "시작 시간", example = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "종료 시간", example = "HH:mm")
    private LocalTime endTime;

    @Schema(description = "예약 가능 여부", example = "true")
    private boolean isAvailable;

    private SlotDto(LocalTime startTime, boolean isAvailable){
        this.startTime = startTime;
        this.endTime = startTime.plusMinutes(30);
        this.isAvailable = isAvailable;
    }
    public static SlotDto of(LocalTime startTime, boolean isAvailable){
        return new SlotDto(startTime, isAvailable);
    }
}