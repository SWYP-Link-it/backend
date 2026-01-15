package org.swyp.linkit.global.error.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidationError {

    private final String field;
    private final String message;
    private final String rejectedValue;

}
