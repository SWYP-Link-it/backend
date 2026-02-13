package org.swyp.linkit.global.error.exception;


import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ChatFileTypeNotSupportedException extends BusinessException {

    public ChatFileTypeNotSupportedException() {
        super(ErrorCode.CHAT_FILE_TYPE_NOT_SUPPORTED);
    }
}