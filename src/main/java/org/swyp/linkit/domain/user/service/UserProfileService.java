package org.swyp.linkit.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.linkit.domain.credit.service.CreditService;
import org.swyp.linkit.domain.user.dto.AvailableScheduleDto;
import org.swyp.linkit.domain.user.dto.UserProfileDto;
import org.swyp.linkit.domain.user.dto.UserSkillDto;
import org.swyp.linkit.domain.user.dto.response.UserProfileResponseDto;
import org.swyp.linkit.domain.user.entity.AvailableSchedule;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserProfile;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.entity.UserSkillImage;
import org.swyp.linkit.domain.user.entity.Weekday;
import org.swyp.linkit.domain.user.repository.UserProfileRepository;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.error.exception.SkillDurationExceedsAvailableTimeException;
import org.swyp.linkit.global.error.exception.UserNotFoundException;
import org.swyp.linkit.global.error.exception.UserProfileAlreadyExistsException;
import org.swyp.linkit.global.error.exception.UserProfileNotFoundException;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final UserSkillImageUploadService userSkillImageUploadService;

    // 프로필 조회
    public UserProfileResponseDto getProfile(Long userId) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 프로필 조회
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(UserProfileNotFoundException::new);

        return UserProfileResponseDto.from(userProfile);
    }

    // 프로필 수정용 조회 (원본 데이터, 병합 없음)
    public UserProfileResponseDto getProfileForEdit(Long userId) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 프로필 조회
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(UserProfileNotFoundException::new);

        return UserProfileResponseDto.fromRaw(userProfile);
    }

    // 프로필 생성
    @Transactional
    public UserProfileResponseDto createProfile(
            Long userId,
            UserProfileDto profileDto,
            Map<Integer, List<MultipartFile>> skillImages) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 이미 프로필이 있는지 확인
        if (userProfileRepository.existsByUserId(userId)) {
            throw new UserProfileAlreadyExistsException();
        }

        // 3. 이미지 사전 검증
        validateAllImages(skillImages);

        // 4. 스킬 거래 시간과 가능한 시간대 검증 (추가)
        validateSkillDurationWithSchedules(
                profileDto.getSkills(),
                profileDto.getAvailableSchedules()
        );

        // 5. UserProfile 생성
        UserProfile userProfile = UserProfile.create(
                user,
                profileDto.getExperienceDescription(),
                profileDto.getExchangeType(),
                profileDto.getPreferredRegion(),
                profileDto.getDetailedLocation()
        );

        // 6. 저장 (스킬/스케줄 추가 전)
        UserProfile savedProfile = userProfileRepository.save(userProfile);

        // 7. UserSkill 생성 및 이미지 업로드
        List<UserSkillDto> skills = profileDto.getSkills();
        for (int i = 0; i < skills.size(); i++) {
            UserSkillDto skillDto = skills.get(i);

            // 스킬 생성
            userSkillService.createUserSkill(savedProfile.getId(), skillDto);

            // DB에 즉시 반영
            userProfileRepository.flush();

            // 이미지 업로드 및 저장
            if (skillImages != null && skillImages.containsKey(i)) {
                List<MultipartFile> images = skillImages.get(i);

                // 방금 생성한 스킬 조회
                UserSkill createdSkill = savedProfile.getUserSkills().get(i);

                // 각 이미지 업로드 및 저장
                for (int imageIndex = 0; imageIndex < images.size(); imageIndex++) {
                    MultipartFile imageFile = images.get(imageIndex);

                    // NCP에 업로드
                    String imageUrl = userSkillImageUploadService.uploadSkillImage(
                            imageFile,
                            userId,
                            createdSkill.getId()
                    );

                    // 업로드 성공 시 DB에 저장
                    if (imageUrl != null) {
                        UserSkillImage skillImage = UserSkillImage.create(
                                createdSkill,
                                imageUrl,
                                imageIndex + 1,
                                imageFile.getOriginalFilename(),
                                imageFile.getSize()
                        );
                        createdSkill.addImage(skillImage);
                    }
                }
            }
        }

        // 8. AvailableSchedule 생성 및 추가
        profileDto.getAvailableSchedules().forEach(scheduleDto -> {
            availableScheduleService.createSchedule(userId, scheduleDto);
        });

        // 9. 크레딧 1 지급
        creditService.rewardCreditOnProfileSetup(user);

        // 10. User 상태 변경 PROFILE_PENDING → ACTIVE
        user.completeProfile();

        return UserProfileResponseDto.fromRaw(savedProfile);
    }

    // 프로필 수정
    @Transactional
    public UserProfileResponseDto updateProfile(
            Long userId,
            UserProfileDto profileDto,
            Map<Integer, List<MultipartFile>> skillImages) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 프로필 조회
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(UserProfileNotFoundException::new);

        // 3. 이미지 사전 검증
        validateAllImages(skillImages);

        // 4. 스킬 거래 시간과 가능한 시간대 검증 (스킬/스케줄이 있을 때만)
        if (!profileDto.getSkills().isEmpty() && !profileDto.getAvailableSchedules().isEmpty()) {
            validateSkillDurationWithSchedules(
                    profileDto.getSkills(),
                    profileDto.getAvailableSchedules()
            );
        }

        // 5. 스킬 수정 (차이 계산)
        updateUserSkills(userProfile, profileDto.getSkills(), skillImages, userId);

        // 6. 스킬이 모두 삭제되었는지 확인
        boolean hasNoSkills = userProfile.getUserSkills().isEmpty();

        if (hasNoSkills) {
            // 6-1. 스킬이 없으면 모든 스케줄 삭제
            List<AvailableSchedule> allSchedules = new ArrayList<>(user.getAvailableSchedules());
            allSchedules.forEach(user::removeAvailableSchedule);

            // 6-2. 선호 지역 정보도 삭제
            userProfile.update(
                    profileDto.getExperienceDescription(),
                    profileDto.getExchangeType(),
                    null,  // preferredRegion 삭제
                    null   // detailedLocation 삭제
            );
        } else {
            // 6-3. 스킬이 있으면 기본 정보 및 스케줄 정상 수정
            userProfile.update(
                    profileDto.getExperienceDescription(),
                    profileDto.getExchangeType(),
                    profileDto.getPreferredRegion(),
                    profileDto.getDetailedLocation()
            );

            // 7. 스케줄 수정 (차이 계산)
            updateAvailableSchedules(user, profileDto.getAvailableSchedules());
        }

        // 8. ResponseDto 변환 및 반환
        return UserProfileResponseDto.fromRaw(userProfile);
    }

    // 스킬 거래 시간과 가능한 시간대 검증
    private void validateSkillDurationWithSchedules(
            List<UserSkillDto> skills,
            List<AvailableScheduleDto> schedules) {

        if (skills == null || skills.isEmpty() || schedules == null || schedules.isEmpty()) {
            return;
        }

        // 1. 가장 긴 스킬 거래 시간 찾기
        int maxSkillDuration = skills.stream()
                .map(UserSkillDto::getExchangeDuration)
                .max(Integer::compareTo)
                .orElse(0);

        // 2. 요일별로 그룹화하여 연속된 시간대 병합
        Map<Weekday, List<AvailableScheduleDto>> schedulesByDay = schedules.stream()
                .collect(Collectors.groupingBy(AvailableScheduleDto::getDayOfWeek));

        // 3. 각 요일별로 연속된 시간대를 병합하여 최대 가용 시간 계산
        int maxAvailableMinutes = schedulesByDay.entrySet().stream()
                .mapToInt(entry -> {
                    Weekday day = entry.getKey();
                    List<AvailableScheduleDto> daySchedules = entry.getValue();

                    // 시간순 정렬
                    daySchedules.sort(Comparator.comparing(AvailableScheduleDto::getStartTime));

                    // 연속된 시간대 병합
                    int maxContinuousMinutes = calculateMaxContinuousMinutes(daySchedules);

                    return maxContinuousMinutes;
                })
                .max()
                .orElse(0);

        // 4. 가장 긴 스킬 거래 시간이 가장 긴 가용 시간보다 긴 경우 예외 발생
        if (maxSkillDuration > maxAvailableMinutes) {
            throw new SkillDurationExceedsAvailableTimeException();
        }
    }

    // 연속된 시간대를 병합하여 최대 연속 시간 계산
    private int calculateMaxContinuousMinutes(List<AvailableScheduleDto> sortedSchedules) {
        if (sortedSchedules.isEmpty()) {
            return 0;
        }

        int maxContinuous = 0;
        int currentContinuous = 0;
        LocalTime currentEnd = null;

        for (AvailableScheduleDto schedule : sortedSchedules) {
            LocalTime start = schedule.getStartTime();
            LocalTime end = schedule.getEndTime();

            int duration;
            // 24:00 처리
            if (end.equals(LocalTime.MIDNIGHT) && start.isAfter(LocalTime.MIDNIGHT)) {
                duration = (int) Duration.between(start, LocalTime.MAX).toMinutes() + 1;
                end = LocalTime.MAX; // 연속된 시간 계산을 위해 end도 변경
            } else {
                duration = (int) Duration.between(start, end).toMinutes();
            }

            // 연속된 시간대인지 확인
            if (currentEnd != null && start.equals(currentEnd)) {
                // 연속됨: 시간 누적
                currentContinuous += duration;
            } else {
                // 연속되지 않음: 이전까지의 최대값 갱신 후 새로 시작
                maxContinuous = Math.max(maxContinuous, currentContinuous);
                currentContinuous = duration;
            }

            currentEnd = end;
        }

        // 마지막 연속 구간 체크
        maxContinuous = Math.max(maxContinuous, currentContinuous);

        return maxContinuous;
    }

    // 시간대의 가용 시간(분) 계산 (기존 메서드는 유지하되 사용 안 함)
    private int calculateAvailableMinutes(LocalTime startTime, LocalTime endTime) {
        // endTime이 00:00(자정)인 경우, 24:00로 처리
        if (endTime.equals(LocalTime.MIDNIGHT) && startTime.isAfter(LocalTime.MIDNIGHT)) {
            // 24:00 - startTime (예: 23:00 ~ 24:00 = 60분)
            return (int) Duration.between(startTime, LocalTime.MAX).toMinutes() + 1;
        }

        // 일반적인 경우
        return (int) Duration.between(startTime, endTime).toMinutes();
    }

    // 스킬 차이 계산 및 반영 (이미지 포함)
    private void updateUserSkills(
            UserProfile userProfile,
            List<UserSkillDto> newSkills,
            Map<Integer, List<MultipartFile>> skillImages,
            Long userId) {

        // null이면 빈 리스트로 처리
        if (newSkills == null) {
            newSkills = new ArrayList<>();
        }

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

        // 이미지 삭제 후 스킬 삭제
        skillsToDelete.forEach(skill -> {
            // 기존 이미지들 NCP에서 삭제
            skill.getImages().forEach(image ->
                    userSkillImageUploadService.deleteImage(image.getImageUrl()));

            // 스킬 삭제
            userProfile.removeUserSkill(skill);
        });

        // 2. 추가/수정
        for (int i = 0; i < newSkills.size(); i++) {
            UserSkillDto skillDto = newSkills.get(i);

            if (skillDto.getId() == null) {
                // 신규 생성
                userSkillService.createUserSkill(userProfile.getId(), skillDto);

                // DB에 즉시 반영
                userProfileRepository.flush();

                // 새로 생성한 스킬 조회
                UserSkill createdSkill = userProfile.getUserSkills()
                        .get(userProfile.getUserSkills().size() - 1);

                // 이미지 업로드
                uploadSkillImages(createdSkill, skillImages, i, userId);

            } else {
                // 기존 수정
                userSkillService.updateUserSkill(userProfile.getId(), skillDto.getId(), skillDto);

                // 기존 스킬 조회
                UserSkill existingSkill = existingSkillMap.get(skillDto.getId());

                if (existingSkill != null) {
                    // 새 파일이 전달된 경우에만 이미지 교체
                    if (skillImages != null && skillImages.containsKey(i)) {
                        // 기존 이미지 삭제
                        existingSkill.getImages().forEach(image ->
                                userSkillImageUploadService.deleteImage(image.getImageUrl()));
                        existingSkill.clearImages();

                        // 새 이미지 업로드
                        uploadSkillImages(existingSkill, skillImages, i, userId);
                    }
                }
            }
        }
    }

    // 스킬 이미지 업로드
    private void uploadSkillImages(
            UserSkill userSkill,
            Map<Integer, List<MultipartFile>> skillImages,
            int skillIndex,
            Long userId) {

        if (skillImages == null || !skillImages.containsKey(skillIndex)) {
            return;
        }

        List<MultipartFile> images = skillImages.get(skillIndex);

        // 각 이미지 업로드
        for (int imageIndex = 0; imageIndex < images.size(); imageIndex++) {
            MultipartFile imageFile = images.get(imageIndex);

            // NCP에 업로드
            String imageUrl = userSkillImageUploadService.uploadSkillImage(
                    imageFile,
                    userId,
                    userSkill.getId()
            );

            // 업로드 성공 시 DB에 저장
            if (imageUrl != null) {
                UserSkillImage skillImage = UserSkillImage.create(
                        userSkill,
                        imageUrl,
                        imageIndex + 1,
                        imageFile.getOriginalFilename(),
                        imageFile.getSize()
                );
                userSkill.addImage(skillImage);
            }
        }
    }

    // 스케줄 차이 계산 및 반영
    private void updateAvailableSchedules(User user, List<AvailableScheduleDto> newSchedules) {
        // null이면 빈 리스트로 처리
        if (newSchedules == null) {
            newSchedules = new ArrayList<>();
        }

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

                // DB에 즉시 반영
                userRepository.flush();

            } else {
                // 기존 수정
                availableScheduleService.updateSchedule(user.getId(), scheduleDto.getId(), scheduleDto);
            }
        });
    }

    // 모든 이미지 사전 검증
    private void validateAllImages(Map<Integer, List<MultipartFile>> skillImages) {
        if (skillImages == null || skillImages.isEmpty()) {
            return;
        }

        for (Map.Entry<Integer, List<MultipartFile>> entry : skillImages.entrySet()) {
            List<MultipartFile> images = entry.getValue();

            userSkillImageUploadService.validateImageCount(images.size());

            for (MultipartFile image : images) {
                userSkillImageUploadService.validateImageOnly(image);
            }
        }
    }
}