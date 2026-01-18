package org.swyp.linkit.global.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.swyp.linkit.global.auth.jwt.JwtTokenProvider;
import org.swyp.linkit.global.auth.jwt.dto.JwtTokenDto;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // 1. CustomOAuth2User에서 User 정보 추출
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Long userId = oAuth2User.getUserId();

        // 2. JWT 토큰 생성
        JwtTokenDto tokenDto = jwtTokenProvider.generateTokenByUserId(userId);

        // 3. 프론트엔드로 리다이렉트=
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", tokenDto.getAccessToken())
                .queryParam("refreshToken", tokenDto.getRefreshToken())
                .build()
                .toUriString();

        response.sendRedirect(targetUrl);
    }
}