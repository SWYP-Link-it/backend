package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class AlreadyWithdrawnException extends BusinessException {
    public AlreadyWithdrawnException() {
        super(ErrorCode.ALREADY_WITHDRAWN);
    }

    public AlreadyWithdrawnException(String message) {
        super(ErrorCode.ALREADY_WITHDRAWN, message);
    }
}