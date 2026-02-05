package org.swyp.linkit.global.swagger.docs;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.code.BaseErrorCode;
import org.swyp.linkit.global.swagger.annotation.ExceptionDoc;
import org.swyp.linkit.global.swagger.exception.SwaggerExampleExceptions;

/**
 * 교환(SkillExchange) 도메인 API 예외 문서
 */
@ExceptionDoc
public class SkillExchangeExceptionDocs {

    /**
     * getReceiverSkillDetails
     */
    public static class GetReceiverSkillDetails{
        public static class UnauthorizedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.UNAUTHORIZED;
            }
        }
    }

    /**
     * getAvailableDates
     */
    public static class GetAvailableDates{
        public static class UnauthorizedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.UNAUTHORIZED;
            }
        }

        public static class MentorNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_MENTOR_NOT_FOUND;
            }
        }

        public static class ScheduleNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_SCHEDULE_NOT_FOUND;
            }
        }
    }

    /**
     * getAvailableSlots
     */
    public static class GetAvailableSlots{
        public static class UnauthorizedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.UNAUTHORIZED;
            }
        }

        public static class UserSkillNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.USER_SKILL_NOT_FOUND;
            }
        }

        public static class MentorNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_MENTOR_NOT_FOUND;
            }
        }
    }

    /**
     * CreateExchange
     */
    public static class CreateExchange{

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

        public static class UserSkillNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.USER_SKILL_NOT_FOUND;
            }
        }

        public static class MentorNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_MENTOR_NOT_FOUND;
            }
        }

        public static class SkillNotAvailableException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_SKILL_NOT_AVAILABLE;
            }
        }

        public static class SelfExchangeNotAllowedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_SELF_REQUEST_NOT_ALLOWED;
            }
        }
        public static class SkillMentorMissMatchException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_SKILL_MENTOR_MISS_MATCH;
            }
        }

        public static class OverExchangeDurationMidnightException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_TIME_OVER_MIDNIGHT;
            }
        }

        public static class UnavailableExchangeTimeException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_INVALID_SCHEDULE_TIME;
            }
        }

        public static class NotEnoughCreditException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.NOT_ENOUGH_CREDIT;
            }
        }

        public static class AlreadyBookedExchangeTimeException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_ALREADY_RESERVED_TIME;
            }
        }
    }

    /**
     * cancelSkillExchange
     */
    public static class CancelSkillExchange{
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

        public static class ExchangeAccessDeniedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_ACCESS_DENIED_CANCEL;
            }
        }
        public static class ExchangeCancelNotAllowedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_CANNOT_ALLOWED;
            }
        }
        public static class InvalidExchangeStatusException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_INVALID_STATUS;
            }
        }
        public static class NotFoundCreditException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.NOT_FOUND_CREDIT;
            }
        }
        public static class InvalidCreditAmountException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.INVALID_CREDIT_AMOUNT;
            }
        }
    }

    /**
     * rejectSkillExchange
     */
    public static class RejectSkillExchange{
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

        public static class ExchangeAccessDeniedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_ACCESS_DENIED_CANCEL;
            }
        }

        public static class InvalidExchangeStatusException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_INVALID_STATUS;
            }
        }
        public static class NotFoundCreditException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.NOT_FOUND_CREDIT;
            }
        }
        public static class InvalidCreditAmountException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.INVALID_CREDIT_AMOUNT;
            }
        }
    }

    /**
     * acceptSkillExchange
     */
    public static class AcceptSkillExchange{
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
        public static class ExchangeAccessDeniedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_ACCESS_DENIED_CANCEL;
            }
        }
        public static class InvalidExchangeStatusException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.EXCHANGE_INVALID_STATUS;
            }
        }
    }

    /**
     * Paging
     */
    public static class Paging{
        public static class UnauthorizedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.UNAUTHORIZED;
            }
        }
    }
}