package org.swyp.linkit.domain.market.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.user.dto.response.AvailableScheduleResponseDto;
import org.swyp.linkit.domain.user.dto.response.UserSkillResponseDto;
import org.swyp.linkit.domain.user.entity.ExchangeType;
import org.swyp.linkit.domain.user.entity.PreferredRegion;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserProfile;
import org.swyp.linkit.domain.user.entity.UserSkill;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "스킬 상세 정보 응답 (스킬 + 프로필 전체)")
public class SkillDetailDto {

    // 사용자 정보
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "프로필 사진 URL", example = "https://...")
    private String profileImageUrl;

    @Schema(description = "닉네임", example = "닉네임123")
    private String nickname;

    // 메인 스킬 정보

    @Schema(description = "선택된 스킬 상세 정보")
    private UserSkillResponseDto mainSkill;

    // 프로필 정보

    @Schema(description = "프로필 ID", example = "1")
    private Long profileId;

    @Schema(description = "경력 및 경험", example = "프론트엔드 개발 5년차입니다.")
    private String experienceDescription;

    @Schema(description = "가르친 횟수", example = "5")
    private Integer timesTaught;

    @Schema(description = "교환 방식", example = "BOTH")
    private ExchangeType exchangeType;

    @Schema(description = "선호 지역", example = "SEOUL")
    private PreferredRegion preferredRegion;

    @Schema(description = "세부 위치", example = "강남역 부근")
    private String detailedLocation;

    // 스케줄 정보

    @Schema(description = "교환 가능 시간대 목록")
    private List<AvailableScheduleResponseDto> availableSchedules;

    // 다른 스킬 목록

    @Schema(description = "해당 사용자의 다른 스킬 목록 (현재 스킬 제외)")
    private List<SkillSummaryDto> otherSkills;

    public static SkillDetailDto from(UserSkill userSkill, List<UserSkill> allUserSkills) {
        UserProfile profile = userSkill.getUserProfile();
        User user = profile.getUser();

        return SkillDetailDto.builder()
                // 사용자 기본 정보
                .userId(user.getId())
                .profileImageUrl(user.getProfileImageUrl())
                .nickname(user.getNickname())

                // 메인 스킬 정보
                .mainSkill(UserSkillResponseDto.from(userSkill))

                // 프로필 정보
                .profileId(profile.getId())
                .experienceDescription(profile.getExperienceDescription())
                .timesTaught(profile.getTimesTaught())
                .exchangeType(profile.getExchangeType())
                .preferredRegion(profile.getPreferredRegion())
                .detailedLocation(profile.getDetailedLocation())

                // 스케줄 정보
                .availableSchedules(user.getAvailableSchedules().stream()
                        .map(AvailableScheduleResponseDto::from)
                        .collect(Collectors.toList()))

                // 다른 스킬 목록 (현재 스킬 제외)
                .otherSkills(allUserSkills.stream()
                        .filter(skill -> !skill.getId().equals(userSkill.getId()))
                        .map(SkillSummaryDto::from)
                        .collect(Collectors.toList()))

                .build();
    }
}