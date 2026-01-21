package org.swyp.linkit.global.common.dto;

import lombok.*;
import org.swyp.linkit.global.error.ErrorCode;

@Builder
@Getter
@AllArgsConstructor
public class ApiResponseDto<T> {

    private boolean success;
    private String code;
    private String message;
    private T data;

    // 성공
    public static <T> ApiResponseDto<T> success(String message, T data){
        return ApiResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    // 비즈니스 실패
    public static <T> ApiResponseDto<T> fail(String errorCode, String ErrorMessage) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .code(errorCode)
                .message(ErrorMessage)
                .build();
    }

    // 요청 필드 검증 실패
    public static <T> ApiResponseDto<T> validationFail(T errors) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .code(ErrorCode.INVALID_INPUT_VALUE.getCode())
                .message(ErrorCode.INVALID_INPUT_VALUE.getMessage())
                .data(errors)
                .build();
    }

}