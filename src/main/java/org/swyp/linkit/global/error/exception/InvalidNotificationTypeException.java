package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class InvalidNotificationTypeException extends BusinessException {

    public InvalidNotificationTypeException() {
        super(ErrorCode.INVALID_NOTIFICATION_TYPE);
    }

    public InvalidNotificationTypeException(String message) {
        super(ErrorCode.INVALID_NOTIFICATION_TYPE, message);
    }
}