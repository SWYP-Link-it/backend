package org.swyp.linkit.domain.user.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.user.entity.AvailableSchedule;
import org.swyp.linkit.domain.user.entity.Weekday;
import org.swyp.linkit.domain.user.repository.AvailableScheduleRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvailableScheduleService {

    private final AvailableScheduleRepository availableScheduleRepository;

    // 현재 날짜 기준 2일 후부터 ~ 3개월 후까지의 가능한 날짜 반환
    public List<AvailableScheduleDto> getExpandedSchedules(Long mentorUserId) {

        LocalDate from = LocalDate.now().plusDays(2);
        LocalDate to = LocalDate.now().plusMonths(3);

        List<AvailableSchedule> weeklyRules = availableScheduleRepository.findAllByUserId(mentorUserId);
        if (weeklyRules.isEmpty()) {
            return List.of();
        }

        // 요일별로 묶기
        Map<Weekday, List<AvailableSchedule>> byDay = weeklyRules.stream()
                .collect(Collectors.groupingBy(AvailableSchedule::getDayOfWeek));

        long days = ChronoUnit.DAYS.between(from, to);

        List<AvailableScheduleDto> result = new ArrayList<>();

        // to 날짜 포함
        for (long i = 0; i <= days; i++) {
            LocalDate date = from.plusDays(i);
            Weekday weekday = toWeekday(date);

            List<AvailableSchedule> rules = byDay.getOrDefault(weekday, List.of());
            for (AvailableSchedule rule : rules) {
                result.add(new AvailableScheduleDto(
                        date,
                        weekday.name(),
                        rule.getStartTime(),
                        rule.getEndTime()
                ));
            }
        }

        // 정렬: 날짜 -> 시작시간
        result.sort(Comparator
                .comparing(AvailableScheduleDto::getDate)
                .thenComparing(AvailableScheduleDto::getStartTime));

        return result;
    }

    private Weekday toWeekday(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> Weekday.MON;
            case TUESDAY -> Weekday.TUE;
            case WEDNESDAY -> Weekday.WED;
            case THURSDAY -> Weekday.THU;
            case FRIDAY -> Weekday.FRI;
            case SATURDAY -> Weekday.SAT;
            case SUNDAY -> Weekday.SUN;
        };
    }
}