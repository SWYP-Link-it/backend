package org.swyp.linkit.global.error.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorReason {
    private final int status;
    private final String code;
    private final String reason;
}
