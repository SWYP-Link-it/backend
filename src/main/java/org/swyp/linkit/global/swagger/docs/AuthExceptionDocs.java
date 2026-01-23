package org.swyp.linkit.global.swagger.docs;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.code.BaseErrorCode;
import org.swyp.linkit.global.swagger.annotation.ExceptionDoc;
import org.swyp.linkit.global.swagger.exception.SwaggerExampleExceptions;

/**
 * 인증(Auth) 도메인 API 예외 문서
 */
@ExceptionDoc
public class AuthExceptionDocs {

    public static class InvalidTokenException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.INVALID_TOKEN;
        }
    }

    public static class ExpiredTokenException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.EXPIRED_TOKEN;
        }
    }

    public static class UnauthorizedException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.UNAUTHORIZED;
        }
    }

    public static class UserNotFoundException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.USER_NOT_FOUND;
        }
    }

    public static class DuplicateNicknameException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.DUPLICATE_NICKNAME;
        }
    }

    public static class InvalidInputValueException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.INVALID_INPUT_VALUE;
        }
    }

    public static class InvalidUserStatusException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.INVALID_USER_STATUS;
        }
    }
}
