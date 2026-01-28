package org.swyp.linkit.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.user.entity.Weekday;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "교환 가능 시간 등록 요청")
public class AvailableScheduleRequestDto {

    @Schema(description = "요일 (MON, TUE, WED, THU, FRI, SAT, SUN)", example = "MON")
    @NotNull(message = "요일은 필수입니다.")
    private Weekday dayOfWeek;

    @Schema(description = "시작 시간 (HH:mm 형식)", example = "14:00")
    @NotNull(message = "시작 시간은 필수입니다.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Schema(description = "종료 시간 (HH:mm 형식)", example = "18:00")
    @NotNull(message = "종료 시간은 필수입니다.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
}