package org.swyp.linkit.global.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        // 에러 메시지를 URL 인코딩
        String errorMessage = URLEncoder.encode(
                exception.getLocalizedMessage(),
                StandardCharsets.UTF_8
        );

        // 프론트엔드로 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", errorMessage)
                .build()
                .toUriString();

        response.sendRedirect(targetUrl);
    }
}