package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class UnsupportedOAuthProviderException extends BusinessException {

    public UnsupportedOAuthProviderException() {
        super(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
    }

    public UnsupportedOAuthProviderException(String message) {
        super(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER, message);
    }
}
