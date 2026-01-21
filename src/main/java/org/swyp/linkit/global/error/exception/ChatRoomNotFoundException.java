package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ChatRoomNotFoundException extends BusinessException {

    public ChatRoomNotFoundException() {
        super(ErrorCode.CHAT_ROOM_NOT_FOUND);
    }

    public ChatRoomNotFoundException(Long roomId) {
        super(ErrorCode.CHAT_ROOM_NOT_FOUND, "채팅방을 찾을 수 없습니다. roomId=" + roomId);
    }
}
