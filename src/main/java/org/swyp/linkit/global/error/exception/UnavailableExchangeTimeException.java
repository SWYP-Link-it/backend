package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class UnavailableExchangeTimeException extends BusinessException {
    public UnavailableExchangeTimeException() {
        super(ErrorCode.INVALID_SCHEDULE_TIME);
    }

    public UnavailableExchangeTimeException(String message) {
        super(ErrorCode.INVALID_SCHEDULE_TIME, message);
    }
}
