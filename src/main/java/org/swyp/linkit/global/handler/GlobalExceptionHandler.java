package org.swyp.linkit.global.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.swyp.linkit.global.common.dto.ApiResponse;
import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.dto.ValidationError;
import org.swyp.linkit.global.error.exception.base.BusinessException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException e) {
        log.error("handleBusinessException: {}", e.getMessage());

        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.getCode(), e.getMessage()));
    }

    // 2. 입력값 검증 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ValidationError>>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("handleValidationException: {}", e.getMessage());

        List<ValidationError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationError(
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue().toString())
                )
                .toList();

        ApiResponse<List<ValidationError>> responseDto = ApiResponse.validationFail(errors);
        return ResponseEntity.badRequest().body(responseDto);
    }

    // 3. 그 외 예상치 못한 모든 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("handleException: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(
                        ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}