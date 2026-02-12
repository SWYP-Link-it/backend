package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class ExchangeTypeRequiredWithSkillsException extends BusinessException {

    public ExchangeTypeRequiredWithSkillsException() {
        super(ErrorCode.EXCHANGE_TYPE_REQUIRED_WITH_SKILLS);
    }

    public ExchangeTypeRequiredWithSkillsException(String message) {
        super(ErrorCode.EXCHANGE_TYPE_REQUIRED_WITH_SKILLS, message);
    }
}