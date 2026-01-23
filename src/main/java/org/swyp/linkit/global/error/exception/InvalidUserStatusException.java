package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class InvalidUserStatusException extends BusinessException {

  public InvalidUserStatusException() {
    super(ErrorCode.INVALID_USER_STATUS);
  }

  public InvalidUserStatusException(String message) {
    super(ErrorCode.INVALID_USER_STATUS, message);
  }
}