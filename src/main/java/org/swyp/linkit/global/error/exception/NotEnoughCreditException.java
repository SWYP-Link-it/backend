package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class NotEnoughCreditException extends BusinessException {

    public NotEnoughCreditException() {
        super(ErrorCode.NOT_ENOUGH_CREDIT);
    }

    public NotEnoughCreditException(String message) {
        super(ErrorCode.NOT_ENOUGH_CREDIT, message);
    }
}
