package org.swyp.linkit.global.error.exception;

import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.base.BusinessException;

public class OAuthAuthenticationFailedException extends BusinessException {

    public OAuthAuthenticationFailedException() {
        super(ErrorCode.OAUTH_AUTHENTICATION_FAILED);
    }

    public OAuthAuthenticationFailedException(String message) {
        super(ErrorCode.OAUTH_AUTHENTICATION_FAILED, message);
    }
}