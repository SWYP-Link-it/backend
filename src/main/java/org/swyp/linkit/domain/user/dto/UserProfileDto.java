package org.swyp.linkit.domain.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.user.dto.request.UserProfileRequestDto;
import org.swyp.linkit.domain.user.entity.ExchangeType;
import org.swyp.linkit.domain.user.entity.PreferredRegion;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class UserProfileDto {

    private String experienceDescription;
    private ExchangeType exchangeType;
    private PreferredRegion preferredRegion;
    private String detailedLocation;
    private List<UserSkillDto> skills;
    private List<AvailableScheduleDto> availableSchedules;

    @Builder(access = AccessLevel.PRIVATE)
    private UserProfileDto(String experienceDescription, ExchangeType exchangeType,
                           PreferredRegion preferredRegion, String detailedLocation,
                           List<UserSkillDto> skills, List<AvailableScheduleDto> availableSchedules) {
        this.experienceDescription = experienceDescription;
        this.exchangeType = exchangeType;
        this.preferredRegion = preferredRegion;
        this.detailedLocation = detailedLocation;
        this.skills = skills;
        this.availableSchedules = availableSchedules;
    }

    public static UserProfileDto from(UserProfileRequestDto requestDto) {
        List<UserSkillDto> skillDtos = requestDto.getSkills() != null
                ? requestDto.getSkills().stream()
                .map(UserSkillDto::from)
                .collect(Collectors.toList())
                : null;

        List<AvailableScheduleDto> scheduleDtos = requestDto.getAvailableSchedules() != null
                ? requestDto.getAvailableSchedules().stream()
                .map(AvailableScheduleDto::from)
                .collect(Collectors.toList())
                : null;

        return UserProfileDto.builder()
                .experienceDescription(requestDto.getExperienceDescription())
                .exchangeType(requestDto.getExchangeType())
                .preferredRegion(requestDto.getPreferredRegion())
                .detailedLocation(requestDto.getDetailedLocation())
                .skills(skillDtos)
                .availableSchedules(scheduleDtos)
                .build();
    }
}