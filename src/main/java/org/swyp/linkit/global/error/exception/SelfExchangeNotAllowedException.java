package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class SelfExchangeNotAllowedException extends BusinessException {
    public SelfExchangeNotAllowedException() {
        super(ErrorCode.SELF_REQUEST_NOT_ALLOWED);
    }

    public SelfExchangeNotAllowedException(String message) {
        super(ErrorCode.SELF_REQUEST_NOT_ALLOWED, message);
    }
}
