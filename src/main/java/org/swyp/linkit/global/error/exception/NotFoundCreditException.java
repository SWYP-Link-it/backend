package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class NotFoundCreditException extends BusinessException {
    public NotFoundCreditException() {
        super(ErrorCode.NOT_FOUND_CREDIT);
    }

    public NotFoundCreditException(String message) {
        super(ErrorCode.NOT_FOUND_CREDIT, message);
    }
}
