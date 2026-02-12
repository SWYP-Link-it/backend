package org.swyp.linkit.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.swyp.linkit.global.error.exception.InvalidWeekdayException;

@Getter
@RequiredArgsConstructor
public enum Weekday {
    MON("월"),
    TUE("화"),
    WED("수"),
    THU("목"),
    FRI("금"),
    SAT("토"),
    SUN("일");

    private final String description;

    // 한글 → Enum 변환 (정렬용)
    public static Weekday fromDescription(String description) {
        for (Weekday weekday : values()) {
            if (weekday.description.equals(description)) {
                return weekday;
            }
        }
        throw new InvalidWeekdayException(
                String.format("유효하지 않은 요일입니다: %s", description)
        );
    }
}