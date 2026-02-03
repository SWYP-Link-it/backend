package org.swyp.linkit.domain.market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.market.dto.response.SkillCardResponseDto;
import org.swyp.linkit.domain.market.dto.response.SkillDetailDto;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.repository.UserSkillRepository;
import org.swyp.linkit.global.error.exception.UserSkillNotFoundException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillMarketService {

    private final UserSkillRepository userSkillRepository;

    // 노출 중인 스킬 카드 조회 (최신순)
    public List<SkillCardResponseDto> getVisibleSkills(SkillCategoryType category) {
        List<UserSkill> skills;

        if (category != null) {
            // 카테고리별 조회
            skills = userSkillRepository.findVisibleSkillsByCategory(category);
            log.info("카테고리별 노출 중인 스킬 카드 조회: category={}, count={}",
                    category.getDescription(), skills.size());
        } else {
            // 전체 조회
            skills = userSkillRepository.findAllVisibleSkills();
            log.info("전체 노출 중인 스킬 카드 조회: count={}", skills.size());
        }

        return skills.stream()
                .map(SkillCardResponseDto::from)
                .toList();
    }

    // 스킬 ID로 상세 정보 조회 (스킬 + 프로필 전체)
    public SkillDetailDto getSkillDetail(Long skillId) {
        // 1. 메인 스킬 조회 (이미지 포함)
        UserSkill mainSkill = userSkillRepository.findVisibleSkillDetailById(skillId)
                .orElseThrow(() -> new UserSkillNotFoundException("해당 스킬을 찾을 수 없거나 현재 노출 중이 아닙니다."));

        // 2. 해당 사용자의 모든 스킬 조회 (이미지 포함)
        Long userId = mainSkill.getUserProfile().getUser().getId();
        List<UserSkill> allUserSkills = userSkillRepository.findVisibleSkillsWithImagesByUserId(userId);

        log.info("스킬 상세 정보 조회: skillId={}, userId={}, totalSkillsCount={}",
                skillId, userId, allUserSkills.size());

        // 3. DTO 변환
        return SkillDetailDto.from(mainSkill, allUserSkills);
    }
}