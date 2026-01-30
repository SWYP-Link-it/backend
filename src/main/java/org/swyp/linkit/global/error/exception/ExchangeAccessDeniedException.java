package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ExchangeAccessDeniedException extends BusinessException {
    public ExchangeAccessDeniedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ExchangeAccessDeniedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
