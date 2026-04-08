package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ChatPublishFailedException extends BusinessException {

    public ChatPublishFailedException() {
        super(ErrorCode.CHAT_PUBLISH_FAILED);
    }

    public ChatPublishFailedException(Long roomId) {
        super(ErrorCode.CHAT_PUBLISH_FAILED,
                "채팅 메시지 발행에 실패했습니다. roomId=" + roomId);
    }
}
