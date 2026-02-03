package org.swyp.linkit.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.swyp.linkit.domain.user.dto.request.AvailableScheduleRequestDto;

import java.time.LocalTime;

public class ScheduleTimeValidator implements ConstraintValidator<ValidScheduleTime, AvailableScheduleRequestDto> {

    @Override
    public boolean isValid(AvailableScheduleRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        LocalTime startTime = dto.getStartTime();
        LocalTime endTime = dto.getEndTime();

        // 시작 시간 = 종료 시간
        if (startTime.equals(endTime)) {
            return violation(context, "시작 시간과 종료 시간은 같을 수 없습니다.");
        }

        // 종료 시간이 00:00이고 시작 시간이 00:00보다 이후
        if (endTime.equals(LocalTime.MIDNIGHT) && startTime.isAfter(LocalTime.MIDNIGHT)) {
            return true;
        }

        // 시작 시간 < 종료 시간
        if (!startTime.isBefore(endTime)) {
            return violation(context, "시작 시간은 종료 시간보다 앞서야 합니다.");
        }

        return true;
    }

    private boolean violation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("startTime")
                .addConstraintViolation();
        return false;
    }
}