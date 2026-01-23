package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class NotificationAccessDeniedException extends BusinessException {

    public NotificationAccessDeniedException() {
        super(ErrorCode.NOTIFICATION_ACCESS_DENIED);
    }

    public NotificationAccessDeniedException(String message) {
        super(ErrorCode.NOTIFICATION_ACCESS_DENIED, message);
    }
}