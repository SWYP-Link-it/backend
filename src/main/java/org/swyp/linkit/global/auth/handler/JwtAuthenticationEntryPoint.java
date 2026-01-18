package org.swyp.linkit.global.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.swyp.linkit.global.common.dto.ApiResponse;
import org.swyp.linkit.global.error.ErrorCode;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        // Filter에서 저장한 예외 코드 확인
        ErrorCode errorCode = (ErrorCode) request.getAttribute("exception");

        if (errorCode == null) {
            errorCode = ErrorCode.UNAUTHORIZED;
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getHttpStatus().value());

        ApiResponse<?> errorResponse = ApiResponse.fail(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}