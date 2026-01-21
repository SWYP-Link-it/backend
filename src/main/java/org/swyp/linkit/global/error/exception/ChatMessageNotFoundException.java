package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ChatMessageNotFoundException extends BusinessException {

    public ChatMessageNotFoundException() {
        super(ErrorCode.CHAT_MESSAGE_NOT_FOUND);
    }

    public ChatMessageNotFoundException(Long messageId) {
        super(ErrorCode.CHAT_MESSAGE_NOT_FOUND, "메시지를 찾을 수 없습니다. messageId=" + messageId);
    }
}