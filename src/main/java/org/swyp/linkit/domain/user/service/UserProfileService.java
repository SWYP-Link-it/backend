package org.swyp.linkit.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.credit.service.CreditService;
import org.swyp.linkit.domain.user.dto.UserProfileDto;
import org.swyp.linkit.domain.user.dto.response.UserProfileResponseDto;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserProfile;
import org.swyp.linkit.domain.user.repository.UserProfileRepository;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.error.exception.UserNotFoundException;
import org.swyp.linkit.global.error.exception.UserProfileNotFoundException;

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
}