package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class InvalidExchangeStatusException extends BusinessException {
    public InvalidExchangeStatusException() {
        super(ErrorCode.EXCHANGE_INVALID_STATUS);
    }

    public InvalidExchangeStatusException(String message) {
        super(ErrorCode.EXCHANGE_INVALID_STATUS, message);
    }
}
