package org.swyp.linkit.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.credit.service.CreditService;
import org.swyp.linkit.domain.user.dto.AvailableScheduleDto;
import org.swyp.linkit.domain.user.dto.UserProfileDto;
import org.swyp.linkit.domain.user.dto.UserSkillDto;
import org.swyp.linkit.domain.user.dto.response.UserProfileResponseDto;
import org.swyp.linkit.domain.user.entity.AvailableSchedule;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserProfile;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.repository.UserProfileRepository;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.error.exception.UserNotFoundException;
import org.swyp.linkit.global.error.exception.UserProfileNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserSkillService userSkillService;
    private final AvailableScheduleService availableScheduleService;
    private final CreditService creditService;

    // 프로필 생성
    @Transactional
    public UserProfileResponseDto createProfile(Long userId, UserProfileDto profileDto) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 이미 프로필이 있는지 확인
        if (userProfileRepository.existsByUserId(userId)) {
            throw new UserProfileNotFoundException();
        }

        // 3. UserProfile 생성
        UserProfile userProfile = UserProfile.create(
                user,
                profileDto.getExperienceDescription(),
                profileDto.getExchangeType(),
                profileDto.getPreferredRegion(),
                profileDto.getDetailedLocation()
        );

        // 4. 저장 (스킬/스케줄 추가 전)
        UserProfile savedProfile = userProfileRepository.save(userProfile);

        // 5. UserSkill 생성 및 추가
        profileDto.getSkills().forEach(skillDto -> {
            userSkillService.createUserSkill(savedProfile.getId(), skillDto);
        });

        // 6. AvailableSchedule 생성 및 추가
        profileDto.getAvailableSchedules().forEach(scheduleDto -> {
            availableScheduleService.createSchedule(userId, scheduleDto);
        });

        // 7. 크레딧 1 지급
        creditService.rewardCreditOnProfileSetup(user);

        // 8. User 상태 변경 PROFILE_PENDING → ACTIVE
        user.completeProfile();

        return UserProfileResponseDto.from(savedProfile);
    }

    // 프로필 수정
    @Transactional
    public UserProfileResponseDto updateProfile(Long userId, UserProfileDto profileDto) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 프로필 조회
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(UserProfileNotFoundException::new);

        // 3. 기본 정보 수정
        userProfile.update(
                profileDto.getExperienceDescription(),
                profileDto.getExchangeType(),
                profileDto.getPreferredRegion(),
                profileDto.getDetailedLocation()
        );

        // 4. 스킬 수정 (차이 계산)
        updateUserSkills(userProfile, profileDto.getSkills());

        // 5. 스케줄 수정 (차이 계산)
        updateAvailableSchedules(user, profileDto.getAvailableSchedules());

        // 6. ResponseDto 변환 및 반환
        return UserProfileResponseDto.from(userProfile);
    }

    // 스킬 차이 계산 및 반영
    private void updateUserSkills(UserProfile userProfile, List<UserSkillDto> newSkills) {
        // 기존 스킬 Map (id -> UserSkill)
        Map<Long, UserSkill> existingSkillMap = userProfile.getUserSkills().stream()
                .collect(Collectors.toMap(UserSkill::getId, skill -> skill));

        // 새로 받은 스킬 ID Set
        Set<Long> newSkillIds = newSkills.stream()
                .map(UserSkillDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 1. 삭제: 기존에 있었는데 새 요청에 없는 것
        List<UserSkill> skillsToDelete = existingSkillMap.values().stream()
                .filter(skill -> !newSkillIds.contains(skill.getId()))
                .toList();
        skillsToDelete.forEach(userProfile::removeUserSkill);

        // 2. 추가/수정
        newSkills.forEach(skillDto -> {
            if (skillDto.getId() == null) {
                // 신규 생성
                userSkillService.createUserSkill(userProfile.getId(), skillDto);
            } else {
                // 기존 수정
                userSkillService.updateUserSkill(userProfile.getId(), skillDto.getId(), skillDto);
            }
        });
    }

    // 스케줄 차이 계산 및 반영
    private void updateAvailableSchedules(User user, List<AvailableScheduleDto> newSchedules) {
        // 기존 스케줄 Map (id -> AvailableSchedule)
        Map<Long, AvailableSchedule> existingScheduleMap = user.getAvailableSchedules().stream()
                .collect(Collectors.toMap(AvailableSchedule::getId, schedule -> schedule));

        // 새로 받은 스케줄 ID Set
        Set<Long> newScheduleIds = newSchedules.stream()
                .map(AvailableScheduleDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 1. 삭제: 기존에 있었는데 새 요청에 없는 것
        List<AvailableSchedule> schedulesToDelete = existingScheduleMap.values().stream()
                .filter(schedule -> !newScheduleIds.contains(schedule.getId()))
                .toList();
        schedulesToDelete.forEach(user::removeAvailableSchedule);

        // 2. 추가/수정
        newSchedules.forEach(scheduleDto -> {
            if (scheduleDto.getId() == null) {
                // 신규 생성
                availableScheduleService.createSchedule(user.getId(), scheduleDto);
            } else {
                // 기존 수정
                availableScheduleService.updateSchedule(user.getId(), scheduleDto.getId(), scheduleDto);
            }
        });
    }
}