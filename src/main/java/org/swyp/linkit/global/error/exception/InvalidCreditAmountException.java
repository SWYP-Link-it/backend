package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class InvalidCreditAmountException extends BusinessException {
    public InvalidCreditAmountException() {
        super(ErrorCode.INVALID_CREDIT_AMOUNT);
    }

    public InvalidCreditAmountException(String message) {
        super(ErrorCode.INVALID_CREDIT_AMOUNT, message);
    }
}
