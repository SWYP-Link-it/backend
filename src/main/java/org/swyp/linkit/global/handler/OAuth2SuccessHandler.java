package org.swyp.linkit.global.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserStatus;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.auth.jwt.JwtTokenProvider;
import org.swyp.linkit.global.auth.jwt.dto.JwtTokenDto;
import org.swyp.linkit.global.auth.oauth.CustomAuthorizationRequestRepository;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.auth.oauth.PendingOAuth2UserInfo;
import org.swyp.linkit.global.error.exception.InvalidUserStatusException;
import org.swyp.linkit.global.error.exception.UserNotFoundException;
import org.swyp.linkit.global.util.CookieUtil;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final int MILLISECONDS_TO_SECONDS = 1000;

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;
    private final CustomAuthorizationRequestRepository customAuthorizationRequestRepository;

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String defaultRedirectBase;

    // 허용할 returnUrl origin 목록
    private static final Set<String> ALLOWED_RETURN_URLS = Set.of(
            "http://localhost:3000",
            "http://localhost:3000/",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:3000/",
            "https://app.desklab.kr",
            "https://app.desklab.kr/"
    );

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // 1) OAuth 인가요청에서 returnUrl 꺼내기 (없으면 default)
        String redirectBase = resolveRedirectBase(request, response);

        // OAuth2User 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String targetUrl;

        // 타입에 따라 분기 처리
        if (oAuth2User instanceof PendingOAuth2UserInfo pendingUser) {
            targetUrl = handlePendingUser(pendingUser, response, redirectBase);
        } else if (oAuth2User instanceof CustomOAuth2User customUser) {
            targetUrl = handleExistingUser(customUser, response, redirectBase);
        } else {
            throw new InvalidUserStatusException("알 수 없는 사용자 타입입니다.");
        }

        response.sendRedirect(targetUrl);
    }

    private String resolveRedirectBase(HttpServletRequest request, HttpServletResponse response) {
        // removeAuthorizationRequest()로 꺼내면서 세션에서 삭제까지
        OAuth2AuthorizationRequest authReq =
                customAuthorizationRequestRepository.removeAuthorizationRequest(request, response);

        if (authReq == null) {
            return defaultRedirectBase;
        }

        Object v = authReq.getAdditionalParameters().get(CustomAuthorizationRequestRepository.RETURN_URL);
        if (!(v instanceof String returnUrl) || returnUrl.isBlank()) {
            return defaultRedirectBase;
        }

        // 2. 화이트리스트 검증 (오픈 리다이렉트 방지)
        return ALLOWED_RETURN_URLS.contains(returnUrl) ? returnUrl : defaultRedirectBase;
    }

    // 신규 회원 시 tempToken 발급
    private String handlePendingUser(
            PendingOAuth2UserInfo pendingUser,
            HttpServletResponse response,
            String redirectBase
    ) {
        String sessionId = pendingUser.getSessionId();

        // sessionId를 담은 tempToken 생성
        String tempToken = jwtTokenProvider.generateTempToken(sessionId);

        // tempToken 쿠키 설정
        cookieUtil.addCookie(response, "tempToken", tempToken, jwtTokenProvider.getTempTokenMaxAge());

        return UriComponentsBuilder.fromUriString(redirectBase)
                .path("/auth/callback")
                .queryParam("status", "PENDING")
                .build()
                .toUriString();
    }

    // 기존 회원 처리 시 refreshToken 발급
    private String handleExistingUser(
            CustomOAuth2User customUser,
            HttpServletResponse response,
            String redirectBase
    ) {
        Long userId = customUser.getUserId();

        // User 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 상태 확인
        if (user.getUserStatus() == UserStatus.WITHDRAWN) {
            throw new InvalidUserStatusException("탈퇴한 사용자입니다.");
        }

        // JWT 토큰 발급
        JwtTokenDto tokenDto = jwtTokenProvider.generateTokenByUserId(userId);

        // refreshToken 쿠키 설정
        long refreshTokenMaxAgeSeconds = tokenDto.getRefreshTokenExpiresIn() / MILLISECONDS_TO_SECONDS;
        cookieUtil.addCookie(response, "refreshToken", tokenDto.getRefreshToken(), refreshTokenMaxAgeSeconds);

        return UriComponentsBuilder.fromUriString(redirectBase)
                .path("/auth/callback")
                .queryParam("status", "ACTIVE")
                .build()
                .toUriString();
    }
}