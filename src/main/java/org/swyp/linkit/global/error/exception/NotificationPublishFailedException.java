package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class NotificationPublishFailedException extends BusinessException {

    public NotificationPublishFailedException() {
        super(ErrorCode.NOTIFICATION_PUBLISH_FAILED);
    }

    public NotificationPublishFailedException(Long notificationId) {
        super(ErrorCode.NOTIFICATION_PUBLISH_FAILED,
                "알림 발행에 실패했습니다. notificationId=" + notificationId);
    }
}
