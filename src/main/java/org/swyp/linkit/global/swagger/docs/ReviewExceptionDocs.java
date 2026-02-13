package org.swyp.linkit.global.swagger.docs;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.code.BaseErrorCode;
import org.swyp.linkit.global.swagger.annotation.ExceptionDoc;
import org.swyp.linkit.global.swagger.exception.SwaggerExampleExceptions;

/**
 * 리뷰(Review) 도메인 API 예외 문서
 */
@ExceptionDoc
public class ReviewExceptionDocs {

    /**
     * CreateReview
     */
    public static class CreateReview{
        public static class UnauthorizedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.UNAUTHORIZED;
            }
        }

        public static class ExchangeNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_NOT_FOUND;
            }
        }

        public static class ExchangeReviewAccessDeniedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.REVIEW_ACCESS_DENIED;
            }
        }

        public static class InvalidExchangeStatusException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.REVIEW_INVALID_STATUS;
            }
        }

        public static class ReviewAlreadyExistsException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.REVIEW_ALREADY_EXISTS;
            }
        }
    }

    /**
     * UpdateReview
     */
    public static class UpdateReview{
        public static class UnauthorizedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.UNAUTHORIZED;
            }
        }

        public static class ReviewNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.REVIEW_NOT_FOUND;
            }
        }

        public static class ReviewAccessDeniedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.REVIEW_ACCESS_DENIED;
            }
        }
    }

    /**
     * DeleteReview
     */
    public static class DeleteReview{
        public static class UnauthorizedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.UNAUTHORIZED;
            }
        }

        public static class ReviewNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.REVIEW_NOT_FOUND;
            }
        }

        public static class ReviewAccessDeniedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.REVIEW_ACCESS_DENIED;
            }
        }
    }


    /**
     * GetReceivedReviews
     */
    public static class GetReceivedReviews{
        public static class UnauthorizedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.UNAUTHORIZED;
            }
        }
    }

    /**
     * GetWrittenReviews
     */
    public static class GetWrittenReviews{
        public static class UnauthorizedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.UNAUTHORIZED;
            }
        }
    }
}
