package org.swyp.linkit.global.swagger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API별 실제 발생 가능한 에러만 선택적으로 Swagger에 표시하기 위한 어노테이션
 * ExceptionDoc 어노테이션이 붙은 클래스를 참조합니다.
 * 예: @ApiErrorExceptionsExample(AuthExceptionDocs.class)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorExceptionsExample {
    Class<?> value();
}
