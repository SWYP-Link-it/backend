package org.swyp.linkit.global.error.code;

import org.swyp.linkit.global.error.dto.ErrorReason;

/**
 * 에러 코드 인터페이스
 * 모든 에러 코드 enum은 이 인터페이스를 구현해야 합니다.
 */
public interface BaseErrorCode {
    ErrorReason getErrorReason();
}
