package org.swyp.linkit.domain.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.user.dto.request.AvailableScheduleRequestDto;
import org.swyp.linkit.domain.user.entity.Weekday;

import java.time.LocalTime;

@Getter
public class AvailableScheduleDto {

    private Weekday dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    @Builder(access = AccessLevel.PRIVATE)
    private AvailableScheduleDto(Weekday dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static AvailableScheduleDto from(AvailableScheduleRequestDto requestDto) {
        return AvailableScheduleDto.builder()
                .dayOfWeek(requestDto.getDayOfWeek())
                .startTime(requestDto.getStartTime())
                .endTime(requestDto.getEndTime())
                .build();
    }
}