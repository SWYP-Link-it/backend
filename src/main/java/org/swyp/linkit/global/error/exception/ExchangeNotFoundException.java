package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ExchangeNotFoundException extends BusinessException {
    public ExchangeNotFoundException() {
        super(ErrorCode.EXCHANGE_NOT_FOUND);
    }

    public ExchangeNotFoundException(String message) {
        super(ErrorCode.EXCHANGE_NOT_FOUND, message);
    }
}
