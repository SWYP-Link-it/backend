package org.swyp.linkit.global.swagger.dto;

import io.swagger.v3.oas.models.examples.Example;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExampleHolder {
    private final Example example;
    private final String name;
    private final int code;
}
