package org.swyp.linkit.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.user.dto.UserSkillDto;
import org.swyp.linkit.domain.user.dto.response.UserSkillForExchangeDto;
import org.swyp.linkit.domain.user.dto.response.UserSkillResponseDto;
import org.swyp.linkit.domain.user.entity.SkillCategory;
import org.swyp.linkit.domain.user.entity.UserProfile;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.repository.SkillCategoryRepository;
import org.swyp.linkit.domain.user.repository.UserProfileRepository;
import org.swyp.linkit.domain.user.repository.UserSkillRepository;
import org.swyp.linkit.global.error.exception.SkillCategoryNotFoundException;
import org.swyp.linkit.global.error.exception.UserProfileNotFoundException;
import org.swyp.linkit.global.error.exception.UserSkillNotFoundException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSkillService {

    private final UserProfileRepository userProfileRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final UserSkillRepository userSkillRepository;

    // 사용자 스킬 생성
    @Transactional
    public void createUserSkill(Long userProfileId, UserSkillDto skillDto) {
        // 1. 프로필 조회
        UserProfile userProfile = userProfileRepository.findById(userProfileId)
                .orElseThrow(UserProfileNotFoundException::new);

        // 2. 카테고리 조회
        SkillCategory category = skillCategoryRepository.findByCategoryType(skillDto.getSkillCategoryType())
                .orElseThrow(SkillCategoryNotFoundException::new);

        // 3. UserSkill 생성
        UserSkill userSkill = UserSkill.create(
                category,
                skillDto.getSkillName(),
                skillDto.getSkillTitle(),
                skillDto.getSkillProficiency(),
                skillDto.getSkillDescription(),
                skillDto.getExchangeDuration()
        );

        // 4. 프로필에 추가 (cascade로 자동 저장)
        userProfile.addUserSkill(userSkill);
    }

    // 사용자 스킬 수정
    @Transactional
    public void updateUserSkill(Long userProfileId, Long userSkillId, UserSkillDto skillDto) {
        // 1. 스킬 조회 및 권한 확인
        UserSkill userSkill = userSkillRepository.findByIdAndUserProfileId(userSkillId, userProfileId)
                .orElseThrow(() -> new UserSkillNotFoundException("수정할 스킬을 찾을 수 없거나 권한이 없습니다."));

        // 2. 카테고리 조회 (카테고리 변경 가능)
        SkillCategory category = skillCategoryRepository.findByCategoryType(skillDto.getSkillCategoryType())
                .orElseThrow(SkillCategoryNotFoundException::new);

        // 3. 스킬 정보 수정 (더티 체킹으로 자동 업데이트)
        userSkill.update(
                category,
                skillDto.getSkillName(),
                skillDto.getSkillTitle(),
                skillDto.getSkillProficiency(),
                skillDto.getSkillDescription(),
                skillDto.getExchangeDuration()
        );
    }

    // 사용자 스킬 삭제
    @Transactional
    public void deleteSkill(Long userId, Long skillId) {
        // 1. 스킬 조회 및 권한 확인
        UserSkill userSkill = userSkillRepository.findByIdAndUserId(skillId, userId)
                .orElseThrow(() -> new UserSkillNotFoundException("삭제할 스킬을 찾을 수 없거나 권한이 없습니다."));

        // 2. 스킬 삭제
        UserProfile userProfile = userSkill.getUserProfile();
        userProfile.removeUserSkill(userSkill);

        log.info("스킬 삭제 완료: userId={}, skillId={}, skillName={}",
                userId, skillId, userSkill.getSkillName());
    }

    // 스킬 노출 토글
    @Transactional
    public UserSkillResponseDto toggleVisibility(Long userId, Long skillId) {
        // 1. 스킬 조회
        UserSkill userSkill = userSkillRepository.findByIdAndUserId(skillId, userId)
                .orElseThrow(UserSkillNotFoundException::new);

        // 2. 노출 토글
        userSkill.toggleVisibility();

        log.info("스킬 노출 상태 변경: userId={}, skillId={}, isVisible={}",
                userId, skillId, userSkill.getIsVisible());

        return UserSkillResponseDto.from(userSkill);
    }

    // UserSkill ID로 UserProfile, User 포함하여 조회
    public UserSkill getUserSkillWithProfileAndUser(Long userSkillId) {
        return userSkillRepository.findByIdWithProfileAndUser(userSkillId)
                .orElseThrow(() ->
                        new UserSkillNotFoundException("존재하지 않는 스킬입니다")
                );
    }

    // UserSkill ID로 UserProfile, User 포함하여 조회, 비관적 락 적용
    public UserSkill getUserSkillWithProfileAndUserAndLock(Long userSkillId) {
        return userSkillRepository.findByIdWithProfileAndUserAndLock(userSkillId)
                .orElseThrow(() ->
                        new UserSkillNotFoundException("존재하지 않는 스킬입니다")
                );
    }

    // 특정 사용자의 교환용 스킬 목록 조회
    public List<UserSkillForExchangeDto> getSkillsForExchange(Long userId) {
        List<UserSkill> skills = userSkillRepository.findVisibleSkillsByUserId(userId);

        log.info("교환용 스킬 목록 조회: userId={}, count={}", userId, skills.size());

        return skills.stream()
                .map(UserSkillForExchangeDto::from)
                .toList();
    }
}