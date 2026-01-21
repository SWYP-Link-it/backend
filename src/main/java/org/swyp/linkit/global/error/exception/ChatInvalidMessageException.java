package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ChatInvalidMessageException extends BusinessException {

    public ChatInvalidMessageException() {
        super(ErrorCode.CHAT_INVALID_MESSAGE);
    }

    public ChatInvalidMessageException(Long messageId) {
        super(ErrorCode.CHAT_INVALID_MESSAGE, "해당 채팅방의 메시지가 아닙니다. messageId=" + messageId);
    }
}