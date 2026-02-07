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
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.linkit.domain.user.dto.UserProfileDto;
import org.swyp.linkit.domain.user.dto.request.UserProfileRequestDto;
import org.swyp.linkit.domain.user.dto.response.UserProfileResponseDto;
import org.swyp.linkit.domain.user.dto.response.UserSkillResponseDto;
import org.swyp.linkit.domain.user.service.UserProfileService;
import org.swyp.linkit.domain.user.service.UserSkillService;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.common.dto.ApiResponseDto;
import org.swyp.linkit.global.swagger.annotation.ApiErrorExceptionsExample;
import org.swyp.linkit.global.swagger.docs.UserProfileExceptionDocs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "UserProfile", description = "사용자 프로필 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserSkillService userSkillService;

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
            summary = "프로필 수정용 조회",
            description = "프로필 수정 시 사용할 원본 데이터를 조회합니다. (시간대 병합 없음, ID 포함)"
    )
    @ApiErrorExceptionsExample(UserProfileExceptionDocs.GetProfile.class)
    @GetMapping(value = "/{userId}/edit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<UserProfileResponseDto>> getProfileForEdit(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId) {

        log.info("[UserProfile] GET /profile/{}/edit", userId);

        UserProfileResponseDto profile = userProfileService.getProfileForEdit(userId);

        return ResponseEntity.ok(
                ApiResponseDto.success("프로필을 조회했습니다.", profile)
        );
    }

    @Operation(
            summary = "프로필 생성",
            description = """
                회원가입 후 프로필 정보를 등록합니다. (스킬, 가능 시간, 이미지 포함)

                - 요청 타입: multipart/form-data
                - profile: application/json (프로필 JSON)
                - 스킬 이미지: key = skill-{스킬인덱스}-images 형식으로 전송
                  예) skill-0-images, skill-1-images
                """
    )
    @ApiErrorExceptionsExample(UserProfileExceptionDocs.CreateProfile.class)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<UserProfileResponseDto>> createProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,

            @Parameter(description = "프로필 등록 정보 (JSON)", required = true)
            @Valid @RequestPart("profile") UserProfileRequestDto profileRequest,

            @Parameter(description = "스킬 이미지 파일들 (key: skill-{스킬인덱스}-images)", required = false)
            @RequestParam(required = false) MultiValueMap<String, MultipartFile> files) {

        log.info("[UserProfile] POST /profile : userId={}", oAuth2User.getUserId());

        // 스킬별 이미지 추출
        Map<Integer, List<MultipartFile>> skillImages = extractSkillImages(files);

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
            description = """
                프로필 정보를 수정합니다. (스킬, 가능 시간, 이미지 포함)

                - 요청 타입: multipart/form-data
                - profile: application/json (프로필 JSON)
                - 스킬 이미지: key = skill-{스킬인덱스}-images 형식으로 전송
                  예) skill-0-images, skill-1-images
                """
    )
    @ApiErrorExceptionsExample(UserProfileExceptionDocs.UpdateProfile.class)
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<UserProfileResponseDto>> updateProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,

            @Parameter(description = "프로필 수정 정보 (JSON)", required = true)
            @Valid @RequestPart("profile") UserProfileRequestDto profileRequest,

            @Parameter(description = "스킬 이미지 파일들 (key: skill-{스킬인덱스}-images)", required = false)
            @RequestParam(required = false) MultiValueMap<String, MultipartFile> files) {

        log.info("[UserProfile] PUT /profile : userId={}", oAuth2User.getUserId());

        // 스킬별 이미지 추출
        Map<Integer, List<MultipartFile>> skillImages = extractSkillImages(files);

        UserProfileResponseDto profile = userProfileService.updateProfile(
                oAuth2User.getUserId(),
                UserProfileDto.from(profileRequest),
                skillImages
        );

        return ResponseEntity.ok(
                ApiResponseDto.success("프로필이 수정되었습니다.", profile)
        );
    }

    @Operation(
            summary = "스킬 노출 토글",
            description = "특정 스킬의 장터 노출 여부를 토글합니다."
    )
    @ApiErrorExceptionsExample(UserProfileExceptionDocs.ToggleSkillVisibility.class)
    @PatchMapping(value = "/skills/{skillId}/visibility", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<UserSkillResponseDto>> toggleSkillVisibility(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,

            @Parameter(description = "스킬 ID", required = true)
            @PathVariable Long skillId) {

        log.info("[UserProfile] PATCH /profile/skills/{}/visibility : userId={}",
                skillId, oAuth2User.getUserId());

        UserSkillResponseDto skill = userSkillService.toggleVisibility(
                oAuth2User.getUserId(),
                skillId
        );

        return ResponseEntity.ok(
                ApiResponseDto.success("스킬 노출 상태가 변경되었습니다.", skill)
        );
    }

    @Operation(
            summary = "스킬 삭제",
            description = "등록된 스킬을 삭제합니다."
    )
    @ApiErrorExceptionsExample(UserProfileExceptionDocs.DeleteSkill.class)
    @DeleteMapping(value = "/skills/{skillId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Void>> deleteSkill(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @Parameter(description = "삭제할 스킬 ID", required = true)
            @PathVariable Long skillId) {

        Long userId = principal.getUserId();

        log.info("[UserProfile] DELETE /profile/skills/{} : userId={}", skillId, userId);

        userSkillService.deleteSkill(userId, skillId);

        return ResponseEntity.ok(
                ApiResponseDto.success("스킬이 삭제되었습니다.", null)
        );
    }

    // MultiValueMap에서 스킬별 이미지 추출
    private Map<Integer, List<MultipartFile>> extractSkillImages(MultiValueMap<String, MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new HashMap<>();
        }

        Map<Integer, List<MultipartFile>> skillImages = new HashMap<>();

        // skill-0-images, skill-1-images 형식의 파일들 추출
        files.forEach((key, fileList) -> {

            // "skill-{skillIndex}-images" 형식 검증
            if (key.matches("skill-\\d+-images")) {
                try {
                    // 스킬 인덱스 추출
                    String[] parts = key.split("-");
                    int skillIndex = Integer.parseInt(parts[1]);

                    if (fileList != null && !fileList.isEmpty()) {
                        // 빈 파일 제외
                        List<MultipartFile> validFiles = fileList.stream()
                                .filter(file -> file != null && !file.isEmpty())
                                .toList();

                        if (!validFiles.isEmpty()) {
                            skillImages.put(skillIndex, validFiles);
                        }
                    }
                } catch (NumberFormatException e) {
                    log.warn("잘못된 파일명 형식입니다: {}", key);
                }
            }
        });

        log.debug("스킬별 이미지 추출 완료: {}개 스킬에 이미지 포함", skillImages.size());
        return skillImages;
    }
}