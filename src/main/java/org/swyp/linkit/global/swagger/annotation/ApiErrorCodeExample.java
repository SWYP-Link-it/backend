package org.swyp.linkit.global.swagger.annotation;

import org.swyp.linkit.global.error.code.BaseErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 도메인별 에러 코드 enum 전체를 Swagger에 표시하기 위한 어노테이션
 * 예: @ApiErrorCodeExample(ErrorCode.class)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodeExample {
    Class<? extends BaseErrorCode> value();
}
