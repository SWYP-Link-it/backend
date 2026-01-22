package org.swyp.linkit.global.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserStatus;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.auth.jwt.JwtTokenProvider;
import org.swyp.linkit.global.auth.jwt.dto.JwtTokenDto;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.error.exception.InvalidUserStatusException;
import org.swyp.linkit.global.error.exception.UserNotFoundException;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

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

        // 2. User 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String targetUrl;

        // 3. 사용자 상태에 따라 분기 처리
        if (user.getUserStatus() == UserStatus.PROFILE_PENDING) {
            // 신규 회원은 tempToken 발급
            String tempToken = jwtTokenProvider.generateTempToken(userId);

            // tempToken 쿠키 Max-Age 계산 (ms → seconds)
            int tempTokenMaxAge = jwtTokenProvider.getTempTokenMaxAge();

            // tempToken을 HttpOnly 쿠키로 설정
            addCookie(response, "tempToken", tempToken, tempTokenMaxAge, "/auth/complete-registration");

            // 프론트로 리다이렉트 (status=PENDING)
            targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .path("/callback")
                    .queryParam("status", "PENDING")
                    .build()
                    .toUriString();

        } else if (user.getUserStatus() == UserStatus.ACTIVE) {
            // 기존 회원은 JWT 토큰 발급
            JwtTokenDto tokenDto = jwtTokenProvider.generateTokenByUserId(userId);

            // refreshToken 쿠키 Max-Age 계산 (ms → seconds)
            int refreshTokenMaxAge = (int) (tokenDto.getRefreshTokenExpiresIn() / 1000);

            // refreshToken을 HttpOnly 쿠키로 설정
            addCookie(response, "refreshToken", tokenDto.getRefreshToken(), refreshTokenMaxAge, "/");

            // 프론트로 리다이렉트 (status=ACTIVE)
            targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .path("/callback")
                    .queryParam("status", "ACTIVE")
                    .build()
                    .toUriString();
        } else {
            throw new InvalidUserStatusException();
        }

        response.sendRedirect(targetUrl);
    }

    // 쿠키 생성
    private void addCookie(HttpServletResponse response, String name, String value, int maxAge, String path) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}