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

    /**
     * completeRegistration
     */
    public static class CompleteRegistration {
        public static class InvalidInputValueException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.INVALID_INPUT_VALUE;
            }
        }

        public static class JsonSerializationException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.INTERNAL_SERVER_ERROR;
            }
        }

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

        public static class SessionExpiredException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.SESSION_EXPIRED;
            }
        }

        public static class DuplicateNicknameException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.DUPLICATE_NICKNAME;
            }
        }

        public static class NotFoundCreditException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.NOT_FOUND_CREDIT;
            }
        }
    }

    /**
     * issueAccessTokenAfterOAuth
     */
    public static class IssueAccessTokenAfterOAuth {
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

        public static class UserNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.USER_NOT_FOUND;
            }
        }

        public static class InvalidUserStatusException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.INVALID_USER_STATUS;
            }
        }
    }

    /**
     * ReissueTokens
     */
    public static class reissueTokens {
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

        public static class UserNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.USER_NOT_FOUND;
            }
        }

        public static class InvalidUserStatusException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.INVALID_USER_STATUS;
            }
        }
    }

    /**
     * getMe
     */
    public static class GetMe {
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

        public static class InvalidUserStatusException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.INVALID_USER_STATUS;
            }
        }
    }

    /**
     * logout
     */
    public static class Logout {

    }

}
