package org.swyp.linkit.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.user.dto.UserSkillDto;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSkillService {

    private final UserProfileRepository userProfileRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final UserSkillRepository userSkillRepository;

    // 사용자 스킬 생성
    @Transactional
    public UserSkillResponseDto createUserSkill(Long userProfileId, UserSkillDto skillDto) {
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

        // 5. ResponseDto 변환 및 반환
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
}