package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ChatFileUploadFailedException extends BusinessException {

    public ChatFileUploadFailedException() {
        super(ErrorCode.CHAT_FILE_UPLOAD_FAILED);
    }
}
