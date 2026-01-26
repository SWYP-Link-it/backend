package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class JsonSerializationException extends BusinessException {

    public JsonSerializationException() {
        super(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    public JsonSerializationException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
