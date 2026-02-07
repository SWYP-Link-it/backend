package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class InvalidWeekdayException extends BusinessException {
    public InvalidWeekdayException() {
        super(ErrorCode.INVALID_WEEKDAY);
    }

    public InvalidWeekdayException(String message) {
        super(ErrorCode.INVALID_WEEKDAY, message);
    }
}
