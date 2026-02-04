package org.swyp.linkit.global.swagger.docs;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.code.BaseErrorCode;
import org.swyp.linkit.global.swagger.annotation.ExceptionDoc;
import org.swyp.linkit.global.swagger.exception.SwaggerExampleExceptions;

/**
 * 사용자 프로필 도메인 API 예외 문서
 */
@ExceptionDoc
public class UserProfileExceptionDocs {

    /**
     * getProfile
     */
    public static class GetProfile {
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

        public static class UserProfileNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.USER_PROFILE_NOT_FOUND;
            }
        }
    }

    /**
     * createProfile
     */
    public static class CreateProfile {
        public static class UnauthorizedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.UNAUTHORIZED;
            }
        }

        public static class InvalidInputValueException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.INVALID_INPUT_VALUE;
            }
        }

        public static class UserNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.USER_NOT_FOUND;
            }
        }

        public static class UserProfileAlreadyExistsException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.USER_PROFILE_ALREADY_EXISTS;
            }
        }

        public static class SkillCategoryNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.SKILL_CATEGORY_NOT_FOUND;
            }
        }
    }

    /**
     * updateProfile
     */
    public static class UpdateProfile {
        public static class UnauthorizedException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.UNAUTHORIZED;
            }
        }

        public static class InvalidInputValueException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.INVALID_INPUT_VALUE;
            }
        }

        public static class UserNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.USER_NOT_FOUND;
            }
        }

        public static class UserProfileNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.USER_PROFILE_NOT_FOUND;
            }
        }

        public static class SkillCategoryNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.SKILL_CATEGORY_NOT_FOUND;
            }
        }

        public static class UserSkillNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.USER_SKILL_NOT_FOUND;
            }
        }

        public static class AvailableScheduleNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.AVAILABLE_SCHEDULE_NOT_FOUND;
            }
        }
    }

    /**
     * toggleSkillVisibility
     */
    public static class ToggleSkillVisibility {
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
    }

    /**
     * updateNickname
     */
    public static class UpdateNickname {
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
    }
}