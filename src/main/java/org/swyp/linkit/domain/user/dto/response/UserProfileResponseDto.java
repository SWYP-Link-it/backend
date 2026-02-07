package org.swyp.linkit.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.user.entity.AvailableSchedule;
import org.swyp.linkit.domain.user.entity.ExchangeType;
import org.swyp.linkit.domain.user.entity.PreferredRegion;
import org.swyp.linkit.domain.user.entity.UserProfile;
import org.swyp.linkit.domain.user.entity.Weekday;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "사용자 프로필 응답")
public class UserProfileResponseDto {

    @Schema(description = "프로필 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 닉네임", example = "기타맨")
    private String nickname;

    @Schema(description = "경력 및 경험", example = "프론트엔드 개발 5년차입니다.")
    private String experienceDescription;

    @Schema(description = "가르친 횟수", example = "0")
    private Integer timesTaught;

    @Schema(description = "교환 방식", example = "BOTH")
    private ExchangeType exchangeType;

    @Schema(description = "선호 지역", example = "SEOUL")
    private PreferredRegion preferredRegion;

    @Schema(description = "세부 위치", example = "강남역 부근")
    private String detailedLocation;

    @Schema(description = "등록된 스킬 목록")
    private List<UserSkillResponseDto> skills;

    @Schema(description = "교환 가능 시간대 목록")
    private List<AvailableScheduleResponseDto> availableSchedules;

    public static UserProfileResponseDto from(UserProfile userProfile) {
        return UserProfileResponseDto.builder()
                .id(userProfile.getId())
                .userId(userProfile.getUser().getId())
                .nickname(userProfile.getUser().getNickname())
                .experienceDescription(userProfile.getExperienceDescription())
                .timesTaught(userProfile.getTimesTaught())
                .exchangeType(userProfile.getExchangeType())
                .preferredRegion(userProfile.getPreferredRegion())
                .detailedLocation(userProfile.getDetailedLocation())
                .skills(userProfile.getUserSkills().stream()
                        .map(UserSkillResponseDto::from)
                        .collect(Collectors.toList()))
                .availableSchedules(mergeConsecutiveSchedules(userProfile.getUser().getAvailableSchedules()))
                .build();
    }

    // 연속된 시간대를 병합하여 반환
    private static List<AvailableScheduleResponseDto> mergeConsecutiveSchedules(
            List<AvailableSchedule> schedules) {

        if (schedules == null || schedules.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 요일별로 그룹화하고 시작 시간 순 정렬
        Map<Weekday, List<AvailableSchedule>> groupedByDay = schedules.stream()
                .collect(Collectors.groupingBy(
                        AvailableSchedule::getDayOfWeek,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparing(AvailableSchedule::getStartTime))
                                        .collect(Collectors.toList())
                        )
                ));

        List<AvailableScheduleResponseDto> merged = new ArrayList<>();

        // 2. 각 요일별로 병합 처리
        groupedByDay.forEach((weekday, daySchedules) -> {
            if (daySchedules.isEmpty()) {
                return;
            }

            // 첫 번째 스케줄로 시작
            LocalTime mergedStart = daySchedules.get(0).getStartTime();
            LocalTime mergedEnd = daySchedules.get(0).getEndTime();

            for (int i = 1; i < daySchedules.size(); i++) {
                AvailableSchedule next = daySchedules.get(i);

                // 현재 종료 시간과 다음 시작 시간이 연속되면 병합
                if (mergedEnd.equals(next.getStartTime())) {
                    mergedEnd = next.getEndTime();
                } else {
                    // 연속되지 않으면 현재까지 병합된 시간대 저장
                    merged.add(AvailableScheduleResponseDto.of(weekday, mergedStart, mergedEnd));

                    // 새로운 시간대 시작
                    mergedStart = next.getStartTime();
                    mergedEnd = next.getEndTime();
                }
            }

            // 마지막 병합된 시간대 추가
            merged.add(AvailableScheduleResponseDto.of(weekday, mergedStart, mergedEnd));
        });

        // 3. 요일 순서대로 정렬 (월~일)
        return merged.stream()
                .sorted(Comparator.comparing(dto ->
                        Weekday.fromDescription(dto.getDayOfWeek())))
                .collect(Collectors.toList());
    }
}