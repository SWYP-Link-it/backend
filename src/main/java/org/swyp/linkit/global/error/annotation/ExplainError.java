package org.swyp.linkit.global.error.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 에러 코드에 대한 설명을 제공하는 어노테이션
 * Swagger 문서화에서 에러 응답 예시의 설명으로 사용됩니다.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExplainError {
    String value() default "";
}
