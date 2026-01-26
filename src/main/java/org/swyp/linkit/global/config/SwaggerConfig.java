package org.swyp.linkit.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.swyp.linkit.global.error.annotation.ExplainError;
import org.swyp.linkit.global.error.code.BaseErrorCode;
import org.swyp.linkit.global.error.dto.ErrorReason;
import org.swyp.linkit.global.swagger.annotation.ApiErrorCodeExample;
import org.swyp.linkit.global.swagger.annotation.ApiErrorExceptionsExample;
import org.swyp.linkit.global.swagger.dto.ExampleHolder;
import org.swyp.linkit.global.swagger.exception.SwaggerExampleExceptions;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LinkIt Swagger Test API")
                        .version("v1")
                        .description("Link-It API"));
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            // @ApiErrorExceptionsExample 처리
            ApiErrorExceptionsExample exceptionsExample =
                    handlerMethod.getMethodAnnotation(ApiErrorExceptionsExample.class);
            if (exceptionsExample != null) {
                generateExceptionResponseExample(operation, exceptionsExample.value());
            }

            // @ApiErrorCodeExample 처리
            ApiErrorCodeExample errorCodeExample =
                    handlerMethod.getMethodAnnotation(ApiErrorCodeExample.class);
            if (errorCodeExample != null) {
                generateErrorCodeResponseExample(operation, errorCodeExample.value());
            }

            return operation;
        };
    }

    /**
     * @ApiErrorExceptionsExample 어노테이션을 처리하여 Swagger 응답 예시 생성
     */
    private void generateExceptionResponseExample(Operation operation, Class<?> exceptionDocClass) {
        // ExceptionDoc 클래스의 모든 inner class 조회
        Class<?>[] innerClasses = exceptionDocClass.getDeclaredClasses();

        // SwaggerExampleExceptions를 구현한 inner class에서 에러 코드 추출
        List<ExampleHolder> exampleHolders = Arrays.stream(innerClasses)
                .filter(SwaggerExampleExceptions.class::isAssignableFrom)
                .map(this::createExampleHolderFromExceptionClass)
                .collect(Collectors.toList());

        addExamplesToOperation(operation, exampleHolders);
    }

    /**
     * @ApiErrorCodeExample 어노테이션을 처리하여 Swagger 응답 예시 생성
     */
    private void generateErrorCodeResponseExample(Operation operation, Class<? extends BaseErrorCode> errorCodeClass) {
        BaseErrorCode[] errorCodes = errorCodeClass.getEnumConstants();
        if (errorCodes == null) {
            return;
        }

        List<ExampleHolder> exampleHolders = Arrays.stream(errorCodes)
                .map(this::createExampleHolderFromErrorCode)
                .collect(Collectors.toList());

        addExamplesToOperation(operation, exampleHolders);
    }

    /**
     * SwaggerExampleExceptions 구현 클래스에서 ExampleHolder 생성
     */
    private ExampleHolder createExampleHolderFromExceptionClass(Class<?> exceptionClass) {
        try {
            SwaggerExampleExceptions exceptionInstance =
                    (SwaggerExampleExceptions) exceptionClass.getDeclaredConstructor().newInstance();
            BaseErrorCode errorCode = exceptionInstance.getErrorCode();
            ErrorReason errorReason = errorCode.getErrorReason();

            // @ExplainError 어노테이션에서 설명 추출
            String description = getExplainErrorDescription(errorCode);

            Example example = new Example();
            example.setValue(createErrorResponseMap(errorReason));
            example.setDescription(description);

            return ExampleHolder.builder()
                    .example(example)
                    .name(errorCode.toString())
                    .code(errorReason.getStatus())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create example from exception class: " + exceptionClass.getName(), e);
        }
    }

    /**
     * BaseErrorCode에서 ExampleHolder 생성
     */
    private ExampleHolder createExampleHolderFromErrorCode(BaseErrorCode errorCode) {
        ErrorReason errorReason = errorCode.getErrorReason();

        // @ExplainError 어노테이션에서 설명 추출
        String description = getExplainErrorDescription(errorCode);

        Example example = new Example();
        example.setValue(createErrorResponseMap(errorReason));
        example.setDescription(description);

        return ExampleHolder.builder()
                .example(example)
                .name(errorCode.toString())
                .code(errorReason.getStatus())
                .build();
    }

    /**
     * 에러 코드의 @ExplainError 어노테이션에서 설명 추출
     */
    private String getExplainErrorDescription(BaseErrorCode errorCode) {
        try {
            Enum<?> enumValue = (Enum<?>) errorCode;
            Field field = enumValue.getDeclaringClass().getField(enumValue.name());
            ExplainError explainError = field.getAnnotation(ExplainError.class);
            return explainError != null ? explainError.value() : errorCode.getErrorReason().getReason();
        } catch (NoSuchFieldException e) {
            return errorCode.getErrorReason().getReason();
        }
    }

    /**
     * ExampleHolder 목록을 Operation의 responses에 추가
     */
    private void addExamplesToOperation(Operation operation, List<ExampleHolder> exampleHolders) {
        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        // HTTP 상태 코드별로 그룹화
        Map<Integer, List<ExampleHolder>> groupedByStatusCode = exampleHolders.stream()
                .collect(Collectors.groupingBy(ExampleHolder::getCode));

        final ApiResponses finalResponses = responses;
        groupedByStatusCode.forEach((statusCode, holders) -> {
            ApiResponse apiResponse = new ApiResponse();
            Content content = new Content();
            MediaType mediaType = new MediaType();

            holders.forEach(holder ->
                    mediaType.addExamples(holder.getName(), holder.getExample())
            );

            content.addMediaType("application/json", mediaType);
            apiResponse.setContent(content);
            apiResponse.setDescription(getStatusDescription(statusCode));

            finalResponses.addApiResponse(String.valueOf(statusCode), apiResponse);
        });
    }

    /**
     * GlobalExceptionHandler의 ApiResponseDto.fail() 형식과 일치하는 에러 응답 생성
     */
    private Map<String, Object> createErrorResponseMap(ErrorReason errorReason) {
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("success", false);
        response.put("code", errorReason.getCode());
        response.put("message", errorReason.getReason());
        response.put("data", null);
        return response;
    }

    /**
     * HTTP 상태 코드에 대한 설명 반환
     */
    private String getStatusDescription(int statusCode) {
        return switch (statusCode) {
            case 400 -> "잘못된 요청";
            case 401 -> "인증 실패";
            case 403 -> "접근 권한 없음";
            case 404 -> "리소스를 찾을 수 없음";
            case 409 -> "충돌";
            case 500 -> "서버 내부 오류";
            default -> "Error";
        };
    }
}
