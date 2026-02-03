package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class SkillImageFileSizeExceededException extends BusinessException {

    public SkillImageFileSizeExceededException() {
        super(ErrorCode.SKILL_IMAGE_FILE_SIZE_EXCEEDED);
    }

    public SkillImageFileSizeExceededException(String message) {
        super(ErrorCode.SKILL_IMAGE_FILE_SIZE_EXCEEDED, message);
    }
}