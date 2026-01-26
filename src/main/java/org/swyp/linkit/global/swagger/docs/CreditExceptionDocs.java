package org.swyp.linkit.global.swagger.docs;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.code.BaseErrorCode;
import org.swyp.linkit.global.swagger.annotation.ExceptionDoc;
import org.swyp.linkit.global.swagger.exception.SwaggerExampleExceptions;

/**
 * 크레딧(Credit) 도메인 API 예외 문서
 */
@ExceptionDoc
public class CreditExceptionDocs {

    /**
     * GetCreditBalance
     */
    public static class GetCreditBalance{
        public static class UnauthorizedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.UNAUTHORIZED;
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
     * getCreditBalanceWithUserDetails
     */
    public static class GetCreditBalanceWithUserDetails{
        public static class UnauthorizedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.UNAUTHORIZED;
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
     * getCreditHistories
     */
    public static class GetCreditHistories{
        public static class UnauthorizedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.UNAUTHORIZED;
            }
        }

        public static class NotFoundCreditException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.NOT_FOUND_CREDIT;
            }
        }

        public static class InvalidInputValue implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.INVALID_INPUT_VALUE;
            }
        }
    }

}
