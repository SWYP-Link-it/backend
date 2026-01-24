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
import org.swyp.linkit.domain.auth.dto.UserResponseDto;
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
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        response.addCookie(refreshTokenCookie);

        // tempToken 쿠키도 삭제 (혹시 남아있을 수 있으니)
        Cookie tempTokenCookie = new Cookie("tempToken", null);
        tempTokenCookie.setMaxAge(0);
        tempTokenCookie.setPath("/");
        tempTokenCookie.setHttpOnly(true);
        tempTokenCookie.setSecure(true);
        response.addCookie(tempTokenCookie);

        return ResponseEntity.ok(
                ApiResponseDto.success("로그아웃되었습니다.", null)
        );
    }
}
