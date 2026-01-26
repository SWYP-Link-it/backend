package org.swyp.linkit.global.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserStatus;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.auth.jwt.JwtTokenProvider;
import org.swyp.linkit.global.auth.jwt.dto.JwtTokenDto;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.auth.oauth.PendingOAuth2UserInfo;
import org.swyp.linkit.global.error.exception.InvalidUserStatusException;
import org.swyp.linkit.global.error.exception.UserNotFoundException;
import org.swyp.linkit.global.util.CookieUtil;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final int MILLISECONDS_TO_SECONDS = 1000;

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // OAuth2User 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String targetUrl;

        // 타입에 따라 분기 처리
        if (oAuth2User instanceof PendingOAuth2UserInfo pendingUser) {
            // 신규 회원은 이미지/닉네임 입력 페이지로 이동
            targetUrl = handlePendingUser(pendingUser, response);
        } else if (oAuth2User instanceof CustomOAuth2User customUser) {
            // 기존 회원은 메인 페이지로 이동
            targetUrl = handleExistingUser(customUser, response);
        } else {
            throw new InvalidUserStatusException("알 수 없는 사용자 타입입니다.");
        }

        response.sendRedirect(targetUrl);
    }

    // 신규 회원 시 tempToken 발급
    private String handlePendingUser(PendingOAuth2UserInfo pendingUser, HttpServletResponse response) {
        String sessionId = pendingUser.getSessionId();

        // sessionId를 담은 tempToken 생성
        String tempToken = jwtTokenProvider.generateTempToken(sessionId);

        // tempToken 쿠키 설정
        cookieUtil.addCookie(response, "tempToken", tempToken, jwtTokenProvider.getTempTokenMaxAge());

        // 프론트로 리다이렉트 (status=PENDING)
        return UriComponentsBuilder.fromUriString(redirectUri)
                .path("/auth/callback")
                .queryParam("status", "PENDING")
                .build()
                .toUriString();
    }

    // 기존 회원 처리 시 refreshToken 발급
    private String handleExistingUser(CustomOAuth2User customUser, HttpServletResponse response) {
        Long userId = customUser.getUserId();

        // User 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 상태 확인
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new InvalidUserStatusException("활성화된 사용자가 아닙니다.");
        }

        // JWT 토큰 발급
        JwtTokenDto tokenDto = jwtTokenProvider.generateTokenByUserId(userId);

        // refreshToken 쿠키 설정
        long refreshTokenMaxAgeSeconds = tokenDto.getRefreshTokenExpiresIn() / MILLISECONDS_TO_SECONDS;
        cookieUtil.addCookie(response, "refreshToken", tokenDto.getRefreshToken(), refreshTokenMaxAgeSeconds);

        // 프론트로 리다이렉트 (status=ACTIVE)
        return UriComponentsBuilder.fromUriString(redirectUri)
                .path("/auth/callback")
                .queryParam("status", "ACTIVE")
                .build()
                .toUriString();
    }
}