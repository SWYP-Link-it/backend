package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ChatSameUserException extends BusinessException {

    public ChatSameUserException() {
        super(ErrorCode.CHAT_SAME_USER);
    }
}