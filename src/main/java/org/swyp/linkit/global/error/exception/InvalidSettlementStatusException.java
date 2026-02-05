package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class InvalidSettlementStatusException extends BusinessException {
    public InvalidSettlementStatusException() {
        super(ErrorCode.SETTLEMENT_INVALID_STATUS);
    }

    public InvalidSettlementStatusException(String message) {
        super(ErrorCode.SETTLEMENT_INVALID_STATUS, message);
    }
}
