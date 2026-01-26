package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class MentorNotFoundException extends BusinessException {
    public MentorNotFoundException() {
        super(ErrorCode.MENTOR_NOT_FOUND_EXCEPTION);
    }

    public MentorNotFoundException(String message) {
        super(ErrorCode.MENTOR_NOT_FOUND_EXCEPTION, message);
    }
}
