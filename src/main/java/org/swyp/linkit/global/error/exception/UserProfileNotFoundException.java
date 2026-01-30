package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class UserProfileNotFoundException extends BusinessException {

    public UserProfileNotFoundException() {
        super(ErrorCode.USER_PROFILE_NOT_FOUND);
    }

    public UserProfileNotFoundException(String message) {
        super(ErrorCode.USER_PROFILE_NOT_FOUND, message);
    }
}