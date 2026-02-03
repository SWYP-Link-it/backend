package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class SkillImageUploadFailedException extends BusinessException {

    public SkillImageUploadFailedException() {
        super(ErrorCode.SKILL_IMAGE_UPLOAD_FAILED);
    }

    public SkillImageUploadFailedException(String message) {
        super(ErrorCode.SKILL_IMAGE_UPLOAD_FAILED, message);
    }
}