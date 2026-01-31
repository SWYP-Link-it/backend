package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class SettlementNotFoundException extends BusinessException {
    public SettlementNotFoundException() {
        super(ErrorCode.SETTLEMENT_NOT_FOUND);
    }

    public SettlementNotFoundException(String message) {
        super(ErrorCode.SETTLEMENT_NOT_FOUND, message);
    }
}
