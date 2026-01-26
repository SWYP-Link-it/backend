package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ScheduleNotFoundException extends BusinessException {
    public ScheduleNotFoundException() {
        super(ErrorCode.SCHEDULE_NOT_FOUND_EXCEPTION);
    }

    public ScheduleNotFoundException(String message) {
        super(ErrorCode.SCHEDULE_NOT_FOUND_EXCEPTION, message);
    }
}
