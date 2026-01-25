package org.swyp.linkit.global.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.swyp.linkit.global.common.dto.ApiResponseDto;
import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.dto.ValidationError;
import org.swyp.linkit.global.error.exception.base.BusinessException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseDto<?>> handleBusinessException(BusinessException e) {
        log.error("handleBusinessException: {}", e.getMessage());

        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponseDto.fail(errorCode.getCode(), e.getMessage()));
    }

    // 2. 입력값 검증 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<List<ValidationError>>> handleValidationException(MethodArgumentNotValidException e) {
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

        ApiResponseDto<List<ValidationError>> responseDto = ApiResponseDto.validationFail(errors);
        return ResponseEntity.badRequest().body(responseDto);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponseDto<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.fail(ErrorCode.INVALID_INPUT_VALUE.getCode(),
                        ErrorCode.INVALID_INPUT_VALUE.getMessage()));
    }

    // 4. 그 외 예상치 못한 모든 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponseDto<?>> handleException(Exception e) {
        log.error("handleException: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.fail(
                        ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}