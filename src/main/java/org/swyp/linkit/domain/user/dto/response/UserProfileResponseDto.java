//package org.swyp.linkit.domain.user.dto.response;
//
//import io.swagger.v3.oas.annotations.media.Schema;
//import lombok.AccessLevel;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import org.swyp.linkit.domain.user.entity.ExchangeType;
//import org.swyp.linkit.domain.user.entity.PreferredRegion;
//import org.swyp.linkit.domain.user.entity.UserProfile;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Getter
//@Builder
//@AllArgsConstructor(access = AccessLevel.PRIVATE)
//@Schema(description = "사용자 프로필 응답")
//public class UserProfileResponseDto {
//
//    @Schema(description = "프로필 ID", example = "1")
//    private Long id;
//
//    @Schema(description = "사용자 ID", example = "1")
//    private Long userId;
//
//    @Schema(description = "사용자 닉네임", example = "기타맨")
//    private String nickname;
//
//    @Schema(description = "경력 및 경험", example = "프론트엔드 개발 5년차입니다.")
//    private String experienceDescription;
//
//    @Schema(description = "가르친 횟수", example = "0")
//    private Integer timesTaught;
//
//    @Schema(description = "교환 방식", example = "BOTH")
//    private ExchangeType exchangeType;
//
//    @Schema(description = "선호 지역", example = "SEOUL")
//    private PreferredRegion preferredRegion;
//
//    @Schema(description = "세부 위치", example = "강남역 부근")
//    private String detailedLocation;
//
//    @Schema(description = "등록된 스킬 목록")
//    private List<UserSkillResponseDto> skills;
//
//    @Schema(description = "교환 가능 시간대 목록")
//    private List<AvailableScheduleResponseDto> availableSchedules;
//
//    public static UserProfileResponseDto from(UserProfile userProfile) {
//        return UserProfileResponseDto.builder()
//                .id(userProfile.getId())
//                .userId(userProfile.getUser().getId())
//                .nickname(userProfile.getUser().getNickname())
//                .experienceDescription(userProfile.getExperienceDescription())
//                .timesTaught(userProfile.getTimesTaught())
//                .exchangeType(userProfile.getExchangeType())
//                .preferredRegion(userProfile.getPreferredRegion())
//                .detailedLocation(userProfile.getDetailedLocation())
//                .skills(userProfile.getUserSkills().stream()
//                        .map(UserSkillResponseDto::from)
//                        .collect(Collectors.toList()))
//                .availableSchedules(userProfile.getUser().getAvailableSchedules().stream()
//                        .map(AvailableScheduleResponseDto::from)
//                        .collect(Collectors.toList()))
//                .build();
//    }
//}