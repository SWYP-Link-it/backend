package org.swyp.linkit.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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
import org.swyp.linkit.global.util.CookieUtil;

@Tag(name = "Auth", description = "인증 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private static final int MILLISECONDS_TO_SECONDS = 1000;

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @Operation(
            summary = "회원가입 완료",
            description = "소셜 로그인 후 프로필 정보를 입력하여 회원가입을 완료합니다."
    )
    @ApiErrorExceptionsExample(AuthExceptionDocs.CompleteRegistration.class)
    @PostMapping(value = "/complete-registration", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<JwtTokenDto>> completeRegistration(
            @Parameter(description = "임시 토큰 (소셜 로그인 시 발급)", required = true)
            @CookieValue("tempToken") String tempToken,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입 완료 정보 (닉네임, 프로필 이미지 등)"
            )
            @Valid @RequestBody CompleteRegistrationRequestDto request,

            HttpServletResponse response) {

        log.info("[Auth] POST /auth/complete-registration : nickname={}", request.getNickname());

        // 1. 회원가입 완료 처리 및 JWT 토큰 발급
        JwtTokenDto tokenDto = authService.completeRegistration(tempToken, request);

        // 2. tempToken 쿠키 삭제
        cookieUtil.deleteCookie(response, "tempToken");

        // 3. refreshToken 쿠키 설정
        long refreshTokenMaxAgeSeconds = tokenDto.getRefreshTokenExpiresIn() / MILLISECONDS_TO_SECONDS;
        cookieUtil.addCookie(response, "refreshToken", tokenDto.getRefreshToken(), refreshTokenMaxAgeSeconds);

        // 4. accessToken JSON 반환
        return ResponseEntity.ok(
                ApiResponseDto.success("회원가입이 완료되었습니다.", tokenDto)
        );
    }

    @Operation(
            summary = "OAuth 로그인 성공 후 토큰 발급",
            description = "기존 회원이 OAuth 로그인 성공 후 refreshToken으로 accessToken을 발급받습니다."
    )
    @ApiErrorExceptionsExample(AuthExceptionDocs.IssueAccessTokenAfterOAuth.class)
    @GetMapping(value = "/success", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<JwtTokenDto>> issueAccessTokenAfterOAuth(
            @Parameter(description = "리프레시 토큰 (쿠키)", required = true)
            @CookieValue("refreshToken") String refreshToken) {

        log.info("[Auth] GET /auth/success");

        // refreshToken으로 accessToken 발급 (OAuth 로그인 직후)
        JwtTokenDto tokenDto = authService.issueTokensByRefreshToken(refreshToken);

        return ResponseEntity.ok(
                ApiResponseDto.success("인증에 성공했습니다.", tokenDto)
        );
    }

    @Operation(
            summary = "accessToken 재발급",
            description = "만료된 accessToken을 refreshToken을 사용하여 재발급합니다."
    )
    @ApiErrorExceptionsExample(AuthExceptionDocs.reissueTokens.class)
    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<JwtTokenDto>> reissueTokens(
            @Parameter(description = "리프레시 토큰 (쿠키)", required = true)
            @CookieValue("refreshToken") String refreshToken,

            HttpServletResponse response) {

        log.info("[Auth] POST /auth/refresh");

        // 1. 새로운 토큰 발급 (accessToken + refreshToken)
        JwtTokenDto tokenDto = authService.issueTokensByRefreshToken(refreshToken);

        // 2. 새로운 refreshToken 쿠키 설정
        long refreshTokenMaxAgeSeconds = tokenDto.getRefreshTokenExpiresIn() / MILLISECONDS_TO_SECONDS;
        cookieUtil.addCookie(response, "refreshToken", tokenDto.getRefreshToken(), refreshTokenMaxAgeSeconds);

        return ResponseEntity.ok(
                ApiResponseDto.success("토큰이 재발급되었습니다.", tokenDto)
        );
    }

    @Operation(
            summary = "현재 로그인한 사용자 정보 조회",
            description = "JWT 토큰을 통해 현재 로그인한 사용자의 정보를 조회합니다."
    )
    @ApiErrorExceptionsExample(AuthExceptionDocs.GetMe.class)
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getMe(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        log.info("[Auth] GET /auth/me : userId={}", oAuth2User.getUserId());

        UserResponseDto userInfo = authService.getUserInfo(oAuth2User.getUserId());

        return ResponseEntity.ok(
                ApiResponseDto.success("사용자 정보를 조회했습니다.", userInfo)
        );
    }

    @Operation(
            summary = "로그아웃",
            description = "로그아웃하고 refreshToken 쿠키를 삭제합니다."
    )
    @ApiErrorExceptionsExample(AuthExceptionDocs.Logout.class)
    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Void>> logout(HttpServletResponse response) {

        log.info("[Auth] POST /auth/logout");

        // refreshToken 쿠키 삭제
        cookieUtil.deleteCookie(response, "refreshToken");

        return ResponseEntity.ok(
                ApiResponseDto.success("로그아웃되었습니다.", null)
        );
    }
}