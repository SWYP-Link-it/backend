package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class NotificationNotFoundException extends BusinessException {

    public NotificationNotFoundException() {
        super(ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    public NotificationNotFoundException(String message) {
        super(ErrorCode.NOTIFICATION_NOT_FOUND, message);
    }
}