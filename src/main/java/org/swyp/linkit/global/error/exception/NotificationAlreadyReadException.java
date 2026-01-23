package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class NotificationAlreadyReadException extends BusinessException {

    public NotificationAlreadyReadException() {
        super(ErrorCode.NOTIFICATION_ALREADY_READ);
    }

    public NotificationAlreadyReadException(String message) {
        super(ErrorCode.NOTIFICATION_ALREADY_READ, message);
    }
}