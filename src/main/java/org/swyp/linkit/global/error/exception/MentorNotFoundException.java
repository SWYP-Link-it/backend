package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class MentorNotFoundException extends BusinessException {
    public MentorNotFoundException() {
        super(ErrorCode.EXCHANGE_MENTOR_NOT_FOUND);
    }

    public MentorNotFoundException(String message) {
        super(ErrorCode.EXCHANGE_MENTOR_NOT_FOUND, message);
    }
}
