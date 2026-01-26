package org.swyp.linkit.global.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.swyp.linkit.global.error.ErrorCode;

@Builder
@Getter
@AllArgsConstructor
@Schema(description = "공통 응답 형식")
public class ApiResponseDto<T> {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;
    @Schema(description = "에러 코드 (성공 시 null)", example = "null")
    private String code;
    @Schema(description = "응답 메시지", example = "요청이 정상적으로 처리되었습니다.")
    private String message;
    @Schema(description = "응답 데이터")
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