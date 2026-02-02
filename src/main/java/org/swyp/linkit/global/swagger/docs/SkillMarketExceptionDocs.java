package org.swyp.linkit.global.swagger.docs;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.code.BaseErrorCode;
import org.swyp.linkit.global.swagger.exception.SwaggerExampleExceptions;

public class SkillMarketExceptionDocs {
    /**
     * getSkillDetail
     */
    public static class GetSkillDetail {
        public static class UserSkillNotFoundException implements SwaggerExampleExceptions {
            @Override
            public BaseErrorCode getErrorCode() {
                return ErrorCode.USER_SKILL_NOT_FOUND;
            }
        }
    }
}
