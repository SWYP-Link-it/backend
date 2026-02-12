package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ReviewAlreadyExistsException extends BusinessException {
    public ReviewAlreadyExistsException() {
        super(ErrorCode.REVIEW_ALREADY_EXISTS);
    }

    public ReviewAlreadyExistsException(String message) {
        super(ErrorCode.REVIEW_ALREADY_EXISTS, ErrorCode.REVIEW_ALREADY_EXISTS.getMessage() + message);
    }
}
