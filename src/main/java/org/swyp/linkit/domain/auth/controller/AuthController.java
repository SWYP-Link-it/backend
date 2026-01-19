package org.swyp.linkit.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.domain.auth.dto.request.RefreshTokenRequestDto;
import org.swyp.linkit.domain.auth.dto.response.CurrentUserResponseDto;
import org.swyp.linkit.domain.auth.service.AuthService;
import org.swyp.linkit.global.auth.jwt.dto.JwtTokenDto;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.common.dto.ApiResponse;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtTokenDto>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto request) {
        JwtTokenDto tokenDto = authService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(
                ApiResponse.success("토큰이 갱신되었습니다.", tokenDto)
        );
    }

    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserResponseDto>> getCurrentUser(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        CurrentUserResponseDto response = authService.getCurrentUser(oAuth2User.getUserId());

        return ResponseEntity.ok(
                ApiResponse.success("사용자 정보 조회 성공", response)
        );
    }
}