package org.swyp.linkit.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.domain.auth.dto.request.CompleteRegistrationRequestDto;
import org.swyp.linkit.domain.auth.service.AuthService;
import org.swyp.linkit.global.auth.jwt.dto.JwtTokenDto;
import org.swyp.linkit.global.common.dto.ApiResponseDto;
import org.swyp.linkit.global.swagger.annotation.ApiErrorExceptionsExample;
import org.swyp.linkit.global.swagger.docs.AuthExceptionDocs;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

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
        Cookie deleteTempToken = new Cookie("tempToken", null);
        deleteTempToken.setMaxAge(0);
        deleteTempToken.setPath("/");
        deleteTempToken.setHttpOnly(true);
        deleteTempToken.setSecure(true);
        response.addCookie(deleteTempToken);

        // 3. refreshToken 쿠키 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", tokenDto.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (tokenDto.getRefreshTokenExpiresIn() / 1000));
        response.addCookie(refreshTokenCookie);

        // 4. accessToken JSON 반환
        return ResponseEntity.ok(
                ApiResponseDto.success("회원가입이 완료되었습니다.", tokenDto)
        );
    }

    @Operation(summary = "소셜 로그인 성공 후 토큰 발급", description = "기존 회원이 소셜 로그인 성공 후 accessToken을 발급받습니다.")
    @ApiErrorExceptionsExample(AuthExceptionDocs.class)
    @GetMapping("/success")
    public ResponseEntity<ApiResponseDto<JwtTokenDto>> getAccessToken(
            @CookieValue("refreshToken") String refreshToken) {

        // refreshToken 검증 및 accessToken 발급
        JwtTokenDto tokenDto = authService.refreshAccessToken(refreshToken);

        return ResponseEntity.ok(
                ApiResponseDto.success("인증에 성공했습니다.", tokenDto)
        );
    }
}
