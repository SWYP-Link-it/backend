package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class SessionExpiredException extends BusinessException {

    public SessionExpiredException() {
        super(ErrorCode.SESSION_EXPIRED);
    }

    public SessionExpiredException(String message) {
        super(ErrorCode.SESSION_EXPIRED, message);
    }
}
