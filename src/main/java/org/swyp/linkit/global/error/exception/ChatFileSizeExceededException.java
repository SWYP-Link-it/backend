package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ChatFileSizeExceededException extends BusinessException {

    public ChatFileSizeExceededException() {
        super(ErrorCode.CHAT_FILE_SIZE_EXCEEDED);
    }
}