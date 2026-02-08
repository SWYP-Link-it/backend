package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class SkillDurationExceedsAvailableTimeException extends BusinessException {

    public SkillDurationExceedsAvailableTimeException() {
        super(ErrorCode.SKILL_DURATION_EXCEEDS_AVAILABLE_TIME);
    }

    public SkillDurationExceedsAvailableTimeException(String message) {
        super(ErrorCode.SKILL_DURATION_EXCEEDS_AVAILABLE_TIME, message);
    }
}