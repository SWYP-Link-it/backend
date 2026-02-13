package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ReviewAccessDeniedException extends BusinessException {
    public ReviewAccessDeniedException() {
        super(ErrorCode.REVIEW_ACCESS_DENIED);
    }

    public ReviewAccessDeniedException(String message) {
        super(ErrorCode.REVIEW_ACCESS_DENIED, ErrorCode.REVIEW_ACCESS_DENIED.getMessage() + message);
    }
}
