package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class OverExchangeDurationMidnightException extends BusinessException {
    public OverExchangeDurationMidnightException() {
        super(ErrorCode.EXCHANGE_TIME_OVER_MIDNIGHT);
    }

    public OverExchangeDurationMidnightException(String message) {
        super(ErrorCode.EXCHANGE_TIME_OVER_MIDNIGHT, message);
    }
}
