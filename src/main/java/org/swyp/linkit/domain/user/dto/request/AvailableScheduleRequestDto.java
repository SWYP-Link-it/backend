package org.swyp.linkit.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.user.entity.Weekday;
import org.swyp.linkit.global.validation.EndTimeDeserializer;
import org.swyp.linkit.global.validation.ValidScheduleTime;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "교환 가능 시간 등록 요청")
@ValidScheduleTime
public class AvailableScheduleRequestDto {

    @Schema(description = "스케줄 ID (수정 시 필수, 생성 시 null)", example = "1")
    private Long id;

    @Schema(description = "요일 (MON, TUE, WED, THU, FRI, SAT, SUN)", example = "MON")
    @NotNull(message = "요일은 필수입니다.")
    private Weekday dayOfWeek;

    @Schema(description = "시작 시간 (HH:mm 형식)", example = "14:00")
    @NotNull(message = "시작 시간은 필수입니다.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Schema(description = "종료 시간 (HH:mm 형식, 24:00 가능)", example = "18:00")
    @NotNull(message = "종료 시간은 필수입니다.")
    @JsonFormat(pattern = "HH:mm")
    @JsonDeserialize(using = EndTimeDeserializer.class)
    private LocalTime endTime;
}