package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class AvailableScheduleNotFoundException extends BusinessException {

    public AvailableScheduleNotFoundException() {
        super(ErrorCode.AVAILABLE_SCHEDULE_NOT_FOUND);
    }

    public AvailableScheduleNotFoundException(String message) {
        super(ErrorCode.AVAILABLE_SCHEDULE_NOT_FOUND, message);
    }
}
