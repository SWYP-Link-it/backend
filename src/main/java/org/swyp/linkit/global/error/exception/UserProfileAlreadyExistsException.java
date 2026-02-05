package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class UserProfileAlreadyExistsException extends BusinessException {

    public UserProfileAlreadyExistsException() {
        super(ErrorCode.USER_PROFILE_ALREADY_EXISTS);
    }

    public UserProfileAlreadyExistsException(String message) {
        super(ErrorCode.USER_PROFILE_ALREADY_EXISTS, message);
    }
}