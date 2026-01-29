package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class AlreadyBookedExchangeTimeException extends BusinessException {
    public AlreadyBookedExchangeTimeException() {
        super(ErrorCode.EXCHANGE_ALREADY_RESERVED_TIME);
    }

    public AlreadyBookedExchangeTimeException(String message) {
        super(ErrorCode.EXCHANGE_ALREADY_RESERVED_TIME, message);
    }
}
