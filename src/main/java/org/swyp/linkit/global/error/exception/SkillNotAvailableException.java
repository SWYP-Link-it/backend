package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class SkillNotAvailableException extends BusinessException {
    public SkillNotAvailableException() {
        super(ErrorCode.SKILL_NOT_AVAILABLE);
    }

    public SkillNotAvailableException(String message) {
        super(ErrorCode.SKILL_NOT_AVAILABLE, message);
    }
}
