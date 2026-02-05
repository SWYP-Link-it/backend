package org.swyp.linkit.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.domain.user.dto.request.NicknameUpdateRequestDto;
import org.swyp.linkit.domain.user.service.UserService;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.common.dto.ApiResponseDto;
import org.swyp.linkit.global.swagger.annotation.ApiErrorExceptionsExample;
import org.swyp.linkit.global.swagger.docs.UserExceptionDocs;

@Tag(name = "User", description = "사용자 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "닉네임 변경",
            description = "로그인한 사용자의 닉네임을 변경합니다."
    )
    @ApiErrorExceptionsExample(UserExceptionDocs.UpdateNickname.class)
    @PatchMapping(value = "/nickname", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Void>> updateNickname(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Valid @RequestBody NicknameUpdateRequestDto requestDto) {

        Long userId = principal.getUserId();

        log.info("[User] PATCH /user/nickname : userId={}, newNickname={}",
                userId, requestDto.getNickname());

        userService.updateNickname(userId, requestDto.getNickname());

        return ResponseEntity.ok(
                ApiResponseDto.success("닉네임이 변경되었습니다.", null)
        );
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "로그인한 사용자의 회원 탈퇴를 처리합니다. 탈퇴 후에는 서비스를 이용할 수 없습니다."
    )
    @ApiErrorExceptionsExample(UserExceptionDocs.WithdrawUser.class)
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Void>> withdrawUser(
            @AuthenticationPrincipal CustomOAuth2User principal) {

        Long userId = principal.getUserId();

        log.info("[User] DELETE /user : userId={}", userId);

        userService.withdrawUser(userId);

        return ResponseEntity.ok(
                ApiResponseDto.success("회원 탈퇴가 완료되었습니다.", null)
        );
    }
}
