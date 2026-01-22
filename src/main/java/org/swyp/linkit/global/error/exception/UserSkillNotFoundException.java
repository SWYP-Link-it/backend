package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class UserSkillNotFoundException extends BusinessException {

    public UserSkillNotFoundException() {
        super(ErrorCode.USER_SKILL_NOT_FOUND);
    }

    public UserSkillNotFoundException(String message) {
        super(ErrorCode.USER_SKILL_NOT_FOUND, message);
    }
}