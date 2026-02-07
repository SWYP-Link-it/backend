package org.swyp.linkit.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.user.entity.AvailableSchedule;
import org.swyp.linkit.domain.user.entity.Weekday;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "교환 가능 시간 응답")
public class AvailableScheduleResponseDto {

    @Schema(description = "스케줄 ID", example = "1")
    private Long id;

    @Schema(description = "요일 (한글)", example = "월")
    private String dayOfWeek;

    @Schema(description = "시작 시간", example = "14:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "18:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    public static AvailableScheduleResponseDto from(AvailableSchedule schedule) {
        return AvailableScheduleResponseDto.builder()
                .id(schedule.getId())
                .dayOfWeek(schedule.getDayOfWeek().getDescription())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .build();
    }

    // 병합된 스케줄용 생성 메서드 (ID는 null)
    public static AvailableScheduleResponseDto of(
            Weekday weekday,
            LocalTime startTime,
            LocalTime endTime) {
        return AvailableScheduleResponseDto.builder()
                .id(null)  // 병합된 스케줄은 ID 없음
                .dayOfWeek(weekday.getDescription())
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }
}