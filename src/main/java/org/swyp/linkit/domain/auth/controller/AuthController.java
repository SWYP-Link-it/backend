package org.swyp.linkit.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.domain.auth.dto.response.UserResponseDto;
import org.swyp.linkit.domain.auth.dto.request.CompleteRegistrationRequestDto;
import org.swyp.linkit.domain.auth.service.AuthService;
import org.swyp.linkit.global.auth.jwt.dto.JwtTokenDto;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.common.dto.ApiResponseDto;
import org.swyp.linkit.global.swagger.annotation.ApiErrorExceptionsExample;
import org.swyp.linkit.global.swagger.docs.AuthExceptionDocs;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private static final int MILLISECONDS_TO_SECONDS = 1000;

    private final AuthService authService;

    @Operation(summary = "회원가입 완료", description = "소셜 로그인 후 프로필 정보를 입력하여 회원가입을 완료합니다.")
    @ApiErrorExceptionsExample(AuthExceptionDocs.class)
    @PostMapping("/complete-registration")
    public ResponseEntity<ApiResponseDto<JwtTokenDto>> completeRegistration(
            @CookieValue("tempToken") String tempToken,
            @Valid @RequestBody CompleteRegistrationRequestDto request,
            HttpServletResponse response) {

        // 1. 회원가입 완료 처리 및 JWT 토큰 발급
        JwtTokenDto tokenDto = authService.completeRegistration(tempToken, request);

        // 2. tempToken 쿠키 삭제
        deleteCookie(response, "tempToken");

        // 3. refreshToken 쿠키 설정
        setRefreshTokenCookie(response, tokenDto.getRefreshToken(), tokenDto.getRefreshTokenExpiresIn());

        // 4. accessToken JSON 반환
        return ResponseEntity.ok(
                ApiResponseDto.success("회원가입이 완료되었습니다.", tokenDto)
        );
    }

    @Operation(
            summary = "OAuth 로그인 성공 후 토큰 발급",
            description = "기존 회원이 OAuth 로그인 성공 후 refreshToken으로 accessToken을 발급받습니다."
    )
    @ApiErrorExceptionsExample(AuthExceptionDocs.class)
    @GetMapping("/success")
    public ResponseEntity<ApiResponseDto<JwtTokenDto>> getAccessTokenAfterOAuth(
            @CookieValue("refreshToken") String refreshToken) {

        // refreshToken으로 accessToken 발급 (OAuth 로그인 직후)
        JwtTokenDto tokenDto = authService.refreshAccessToken(refreshToken);

        return ResponseEntity.ok(
                ApiResponseDto.success("인증에 성공했습니다.", tokenDto)
        );
    }

    @Operation(
            summary = "accessToken 재발급",
            description = "만료된 accessToken을 refreshToken을 사용하여 재발급합니다."
    )
    @ApiErrorExceptionsExample(AuthExceptionDocs.class)
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDto<JwtTokenDto>> refreshAccessToken(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response) {

        // 1. 새로운 토큰 발급 (accessToken + refreshToken)
        JwtTokenDto tokenDto = authService.refreshAccessToken(refreshToken);

        // 2. 새로운 refreshToken 쿠키 설정
        setRefreshTokenCookie(response, tokenDto.getRefreshToken(), tokenDto.getRefreshTokenExpiresIn());

        return ResponseEntity.ok(
                ApiResponseDto.success("토큰이 재발급되었습니다.", tokenDto)
        );
    }

    @Operation(summary = "현재 로그인한 사용자 정보 조회", description = "JWT 토큰을 통해 현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiErrorExceptionsExample(AuthExceptionDocs.class)
    @GetMapping("/me")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getCurrentUser(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        UserResponseDto userInfo = authService.getCurrentUser(oAuth2User.getUserId());

        return ResponseEntity.ok(
                ApiResponseDto.success("사용자 정보를 조회했습니다.", userInfo)
        );
    }

    @Operation(summary = "로그아웃", description = "로그아웃하고 refreshToken 쿠키를 삭제합니다.")
    @ApiErrorExceptionsExample(AuthExceptionDocs.class)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout(HttpServletResponse response) {

        // refreshToken 쿠키 삭제
        deleteCookie(response, "refreshToken");

        return ResponseEntity.ok(
                ApiResponseDto.success("로그아웃되었습니다.", null)
        );
    }

    // refreshToken 쿠키 설정
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken, long expiresIn) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (expiresIn / MILLISECONDS_TO_SECONDS));
        response.addCookie(cookie);
    }

    // 쿠키 삭제
    private void deleteCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }
}
