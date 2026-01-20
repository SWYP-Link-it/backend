package org.swyp.linkit.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;

import java.time.LocalTime;

@Entity
@Table(
        name = "available_schedule"
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvailableSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "available_schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "day_of_week", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private Weekday dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Builder(access = AccessLevel.PRIVATE)
    private AvailableSchedule(User user, Weekday dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.user = user;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // 가능 일정 생성
    public static AvailableSchedule create(User user, Weekday dayOfWeek, LocalTime startTime, LocalTime endTime) {
        return AvailableSchedule.builder()
                .user(user)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    // 가능 일정 수정
    public void updateSchedule(Weekday dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // 사용자 연관관계 설정
    protected void assignUser(User user) {
        this.user = user;
    }
}
