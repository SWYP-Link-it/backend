package org.swyp.linkit.global.swagger.docs;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.code.BaseErrorCode;
import org.swyp.linkit.global.swagger.annotation.ExceptionDoc;
import org.swyp.linkit.global.swagger.exception.SwaggerExampleExceptions;

/**
 * 알림(Notification) 도메인 API 예외 문서
 */
@ExceptionDoc
public class NotificationExceptionDocs {

    public static class UnauthorizedException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.UNAUTHORIZED;
        }
    }

    public static class NotificationNotFoundException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.NOTIFICATION_NOT_FOUND;
        }
    }

    public static class NotificationAccessDeniedException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.NOTIFICATION_ACCESS_DENIED;
        }
    }

    public static class NotificationAlreadyReadException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.NOTIFICATION_ALREADY_READ;
        }
    }

    public static class InvalidNotificationTypeException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.INVALID_NOTIFICATION_TYPE;
        }
    }
}
