package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ReviewInvalidStatusException extends BusinessException {
    public ReviewInvalidStatusException() {
        super(ErrorCode.REVIEW_INVALID_STATUS);
    }

    public ReviewInvalidStatusException(String message) {
        super(ErrorCode.REVIEW_INVALID_STATUS, ErrorCode.REVIEW_INVALID_STATUS.getMessage() + message);
    }
}
