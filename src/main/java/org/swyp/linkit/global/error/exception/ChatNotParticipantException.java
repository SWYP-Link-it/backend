package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ChatNotParticipantException extends BusinessException {

    public ChatNotParticipantException() {
        super(ErrorCode.CHAT_NOT_PARTICIPANT);
    }

    public ChatNotParticipantException(Long roomId, Long userId) {
        super(ErrorCode.CHAT_NOT_PARTICIPANT, "채팅방 참여자가 아닙니다. roomId=" + roomId + ", userId=" + userId);
    }
}