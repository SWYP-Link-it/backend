package org.swyp.linkit.domain.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.user.entity.Weekday;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ExpandedScheduleDto {

    private LocalDate date;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    @Builder(access = AccessLevel.PRIVATE)
    private ExpandedScheduleDto(LocalDate date, String dayOfWeek,
                                LocalTime startTime, LocalTime endTime) {
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static ExpandedScheduleDto of(LocalDate date, Weekday weekday,
                                         LocalTime startTime, LocalTime endTime) {
        return ExpandedScheduleDto.builder()
                .date(date)
                .dayOfWeek(weekday.getDescription())
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }
}