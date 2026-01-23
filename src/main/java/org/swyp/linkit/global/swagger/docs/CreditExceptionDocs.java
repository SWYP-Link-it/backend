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

    public static class NotEnoughCreditException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.NOT_ENOUGH_CREDIT;
        }
    }

    public static class InvalidCreditAmountException implements SwaggerExampleExceptions {
        @Override
        public BaseErrorCode getErrorCode() {
            return ErrorCode.INVALID_CREDIT_AMOUNT;
        }
    }
}
