package org.swyp.linkit.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ScheduleTimeValidator.class)
@Documented
public @interface ValidScheduleTime {

    String message() default "시작 시간은 종료 시간보다 앞서야 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}