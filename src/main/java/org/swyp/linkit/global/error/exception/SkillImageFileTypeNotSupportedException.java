package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class SkillImageFileTypeNotSupportedException extends BusinessException {

    public SkillImageFileTypeNotSupportedException() {
        super(ErrorCode.SKILL_IMAGE_FILE_TYPE_NOT_SUPPORTED);
    }

    public SkillImageFileTypeNotSupportedException(String message) {
        super(ErrorCode.SKILL_IMAGE_FILE_TYPE_NOT_SUPPORTED, message);
    }
}