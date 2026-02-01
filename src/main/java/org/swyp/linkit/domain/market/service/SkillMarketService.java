package org.swyp.linkit.domain.market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.market.dto.response.SkillCardResponseDto;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.repository.UserSkillRepository;

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
}