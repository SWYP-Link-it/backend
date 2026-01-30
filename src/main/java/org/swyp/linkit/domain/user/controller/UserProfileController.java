package org.swyp.linkit.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.domain.user.dto.UserProfileDto;
import org.swyp.linkit.domain.user.dto.request.UserProfileRequestDto;
import org.swyp.linkit.domain.user.dto.response.UserProfileResponseDto;
import org.swyp.linkit.domain.user.service.UserProfileService;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.common.dto.ApiResponseDto;
import org.swyp.linkit.global.swagger.annotation.ApiErrorExceptionsExample;
import org.swyp.linkit.global.swagger.docs.UserProfileExceptionDocs;

@Tag(name = "UserProfile", description = "사용자 프로필 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Operation(
            summary = "프로필 조회",
            description = "사용자 ID로 프로필 정보를 조회합니다."
    )
    @ApiErrorExceptionsExample(UserProfileExceptionDocs.GetProfile.class)
    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<UserProfileResponseDto>> getProfile(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId) {

        log.info("[UserProfile] GET /profile/{}", userId);

        UserProfileResponseDto profile = userProfileService.getProfile(userId);

        return ResponseEntity.ok(
                ApiResponseDto.success("프로필을 조회했습니다.", profile)
        );
    }

    @Operation(
            summary = "프로필 생성",
            description = "회원가입 후 프로필 정보를 등록합니다. (스킬, 가능 시간 포함)"
    )
    @ApiErrorExceptionsExample(UserProfileExceptionDocs.CreateProfile.class)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<UserProfileResponseDto>> createProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "프로필 등록 정보 (스킬, 가능 시간 포함)"
            )
            @Valid @RequestBody UserProfileRequestDto request) {

        log.info("[UserProfile] POST /profile : userId={}", oAuth2User.getUserId());

        UserProfileResponseDto profile = userProfileService.createProfile(
                oAuth2User.getUserId(),
                UserProfileDto.from(request)
        );

        return ResponseEntity.ok(
                ApiResponseDto.success("프로필이 생성되었습니다.", profile)
        );
    }

    @Operation(
            summary = "프로필 수정",
            description = "프로필 정보를 수정합니다. (스킬, 가능 시간 포함)"
    )
    @ApiErrorExceptionsExample(UserProfileExceptionDocs.UpdateProfile.class)
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<UserProfileResponseDto>> updateProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "프로필 수정 정보 (스킬, 가능 시간 포함)"
            )
            @Valid @RequestBody UserProfileRequestDto request) {

        log.info("[UserProfile] PUT /profile : userId={}", oAuth2User.getUserId());

        UserProfileResponseDto profile = userProfileService.updateProfile(
                oAuth2User.getUserId(),
                UserProfileDto.from(request)
        );

        return ResponseEntity.ok(
                ApiResponseDto.success("프로필이 수정되었습니다.", profile)
        );
    }
}