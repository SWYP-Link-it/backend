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
                return ErrorCode.MENTOR_NOT_FOUND_EXCEPTION;
            }
        }

        public static class ScheduleNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.SCHEDULE_NOT_FOUND_EXCEPTION;
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
                return ErrorCode.MENTOR_NOT_FOUND_EXCEPTION;
            }
        }

    }
}
