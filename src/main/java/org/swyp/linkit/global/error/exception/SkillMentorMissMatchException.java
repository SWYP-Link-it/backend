package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class SkillMentorMissMatchException extends BusinessException {
    public SkillMentorMissMatchException() {
        super(ErrorCode.SKILL_MENTOR_MISS_MATCH);
    }

    public SkillMentorMissMatchException(String message) {
        super(ErrorCode.SKILL_MENTOR_MISS_MATCH, message);
    }
}
