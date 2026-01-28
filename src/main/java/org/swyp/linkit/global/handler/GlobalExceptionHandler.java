package org.swyp.linkit.global.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
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

    // 2. HTTP Method 가 다를때 예외 처리
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiResponseDto<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
                .body(ApiResponseDto.fail(ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                        e.getMethod() + " 메서드는 지원되지 않습니다."));
    }

    // 3. JSON 형식이 잘못되었거나, 파싱에 실패한 경우 예외 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiResponseDto<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.HTTP_MESSAGE_NOT_READABLE.getHttpStatus())
                .body(ApiResponseDto.fail(ErrorCode.HTTP_MESSAGE_NOT_READABLE.getCode(),
                        ErrorCode.HTTP_MESSAGE_NOT_READABLE.getMessage()));
    }

    // 2. DTO 검증 실패 시 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<List<ValidationError>>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("handleValidationException: {}", e.getMessage());

        List<ValidationError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationError(
                        error.getField(),
                        error.getDefaultMessage(),
                        String.valueOf(error.getRejectedValue()))
                )
                .toList();

        ApiResponseDto<List<ValidationError>> responseDto = ApiResponseDto.validationFail(errors);
        return ResponseEntity.badRequest().body(responseDto);
    }

    // 3. @PathVariable이나 @RequestParam의 타입이 일치하지 않는 경우 예외 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponseDto<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.TYPE_MISMATCH.getHttpStatus())
                .body(ApiResponseDto.fail(ErrorCode.TYPE_MISMATCH.getCode(),
                        e.getName() + "의 타입이 잘못되었습니다."));
    }

    // 4. @RequestParam이 누락된 경우 예외 처리
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ApiResponseDto<Void>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("MissingServletRequestParameterException: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.MISSING_INPUT_VALUE.getHttpStatus())
                .body(ApiResponseDto.fail(ErrorCode.MISSING_INPUT_VALUE.getCode(),
                        e.getParameterName() + " 파라미터가 누락되었습니다."));
    }

    // 4. 존재하지 않는 경로(URL)으로 인한 예외 처리
    @ExceptionHandler(NoResourceFoundException.class)
    protected ResponseEntity<ApiResponseDto<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.error("NoResourceFoundException: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.NOT_FOUND.getHttpStatus())
                .body(ApiResponseDto.fail(ErrorCode.NOT_FOUND.getCode(),
                        ErrorCode.NOT_FOUND.getMessage()));
    }

    // 5. 그 외 예상치 못한 모든 예외 처리
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