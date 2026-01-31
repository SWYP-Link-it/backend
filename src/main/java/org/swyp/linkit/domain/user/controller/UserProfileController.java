package org.swyp.linkit.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.swyp.linkit.domain.user.dto.UserProfileDto;
import org.swyp.linkit.domain.user.dto.request.UserProfileRequestDto;
import org.swyp.linkit.domain.user.dto.response.UserProfileResponseDto;
import org.swyp.linkit.domain.user.service.UserProfileService;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.common.dto.ApiResponseDto;
import org.swyp.linkit.global.swagger.annotation.ApiErrorExceptionsExample;
import org.swyp.linkit.global.swagger.docs.UserProfileExceptionDocs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
            description = "회원가입 후 프로필 정보를 등록합니다. (스킬, 가능 시간, 이미지 포함)"
    )
    @ApiErrorExceptionsExample(UserProfileExceptionDocs.CreateProfile.class)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<UserProfileResponseDto>> createProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,

            @Parameter(description = "프로필 등록 정보 (JSON)", required = true)
            @Valid @RequestPart("profile") UserProfileRequestDto profileRequest,

            @Parameter(description = "스킬별 이미지 파일들 (skill-{스킬인덱스}-images 형식)", required = false)
            HttpServletRequest request) {

        log.info("[UserProfile] POST /profile : userId={}", oAuth2User.getUserId());

        // 스킬별 이미지 추출
        Map<Integer, List<MultipartFile>> skillImages = extractSkillImages(request);

        UserProfileResponseDto profile = userProfileService.createProfile(
                oAuth2User.getUserId(),
                UserProfileDto.from(profileRequest),
                skillImages
        );

        return ResponseEntity.ok(
                ApiResponseDto.success("프로필이 생성되었습니다.", profile)
        );
    }

    @Operation(
            summary = "프로필 수정",
            description = "프로필 정보를 수정합니다. (스킬, 가능 시간, 이미지 포함)"
    )
    @ApiErrorExceptionsExample(UserProfileExceptionDocs.UpdateProfile.class)
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<UserProfileResponseDto>> updateProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,

            @Parameter(description = "프로필 수정 정보 (JSON)", required = true)
            @Valid @RequestPart("profile") UserProfileRequestDto profileRequest,

            @Parameter(description = "스킬별 이미지 파일들 (skill-{스킬인덱스}-images 형식)", required = false)
            HttpServletRequest request) {

        log.info("[UserProfile] PUT /profile : userId={}", oAuth2User.getUserId());

        // 스킬별 이미지 추출
        Map<Integer, List<MultipartFile>> skillImages = extractSkillImages(request);

        UserProfileResponseDto profile = userProfileService.updateProfile(
                oAuth2User.getUserId(),
                UserProfileDto.from(profileRequest),
                skillImages
        );

        return ResponseEntity.ok(
                ApiResponseDto.success("프로필이 수정되었습니다.", profile)
        );
    }

    // HttpServletRequest에서 스킬별 이미지 추출
    private Map<Integer, List<MultipartFile>> extractSkillImages(HttpServletRequest request) {
        if (!(request instanceof MultipartHttpServletRequest)) {
            return new HashMap<>();
        }

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<Integer, List<MultipartFile>> skillImages = new HashMap<>();

        // skill-0-images, skill-1-images 형식의 파일들 추출
        Iterator<String> fileNames = multipartRequest.getFileNames();
        while (fileNames.hasNext()) {
            String fileName = fileNames.next();

            // "skill-{skillIndex}-images" 형식 검증
            if (fileName.matches("skill-\\d+-images")) {
                try {
                    // 스킬 인덱스 추출
                    String[] parts = fileName.split("-");
                    int skillIndex = Integer.parseInt(parts[1]);

                    // 해당 스킬의 모든 이미지 가져오기
                    List<MultipartFile> files = multipartRequest.getFiles(fileName);
                    if (files != null && !files.isEmpty()) {
                        // 빈 파일 제외
                        List<MultipartFile> validFiles = files.stream()
                                .filter(file -> file != null && !file.isEmpty())
                                .toList();

                        if (!validFiles.isEmpty()) {
                            skillImages.put(skillIndex, validFiles);
                        }
                    }
                } catch (NumberFormatException e) {
                    log.warn("잘못된 파일명 형식입니다: {}", fileName);
                }
            }
        }

        log.debug("스킬별 이미지 추출 완료: {}개 스킬에 이미지 포함", skillImages.size());
        return skillImages;
    }
}