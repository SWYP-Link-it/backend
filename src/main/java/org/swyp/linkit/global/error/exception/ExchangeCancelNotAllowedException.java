package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ExchangeCancelNotAllowedException extends BusinessException {
    public ExchangeCancelNotAllowedException() {
        super(ErrorCode.EXCHANGE_CANNOT_ALLOWED);
    }

    public ExchangeCancelNotAllowedException(String message) {
        super(ErrorCode.EXCHANGE_CANNOT_ALLOWED, message);
    }
}
