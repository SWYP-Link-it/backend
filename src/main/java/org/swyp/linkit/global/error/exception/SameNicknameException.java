package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class SameNicknameException extends BusinessException {

    public SameNicknameException() {
        super(ErrorCode.SAME_NICKNAME);
    }

    public SameNicknameException(String message) {
        super(ErrorCode.SAME_NICKNAME, message);
    }
}