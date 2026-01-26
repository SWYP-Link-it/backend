package org.swyp.linkit.global.swagger.exception;

import org.swyp.linkit.global.error.code.BaseErrorCode;

/**
 * Swagger 예외 문서화를 위한 마커 인터페이스
 * API별 발생 가능한 예외를 정의하는 inner class들이 이 인터페이스를 구현합니다.
 */
public interface SwaggerExampleExceptions {
    BaseErrorCode getErrorCode();
}
