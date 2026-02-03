package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class SkillImageCountExceededException extends BusinessException {

    public SkillImageCountExceededException() {
        super(ErrorCode.SKILL_IMAGE_COUNT_EXCEEDED);
    }

    public SkillImageCountExceededException(String message) {
        super(ErrorCode.SKILL_IMAGE_COUNT_EXCEEDED, message);
    }
}