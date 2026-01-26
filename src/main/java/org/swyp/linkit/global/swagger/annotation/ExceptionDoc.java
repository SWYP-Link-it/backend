package org.swyp.linkit.global.swagger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 예외 문서 클래스임을 나타내는 마커 어노테이션
 * 이 어노테이션이 붙은 클래스는 SwaggerExampleExceptions를 구현한 inner class들을 포함합니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionDoc {
}
