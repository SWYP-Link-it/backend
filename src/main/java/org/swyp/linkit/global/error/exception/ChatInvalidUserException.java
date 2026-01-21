package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ChatInvalidUserException extends BusinessException {

    public ChatInvalidUserException() {
        super(ErrorCode.CHAT_INVALID_USER);
    }
}