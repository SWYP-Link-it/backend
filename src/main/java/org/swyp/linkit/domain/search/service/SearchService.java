package org.swyp.linkit.domain.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.market.dto.response.SkillCardResponseDto;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.repository.UserSkillRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final UserSkillRepository userSkillRepository;

    // 스킬명으로 검색 (완전 일치)
    public List<SkillCardResponseDto> searchSkills(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            log.warn("검색 키워드가 비어있습니다.");
            return List.of();
        }

        List<UserSkill> skills = userSkillRepository.searchVisibleSkillsByName(keyword.trim());

        log.info("스킬 검색 (완전 일치): keyword='{}', count={}", keyword, skills.size());

        return skills.stream()
                .map(SkillCardResponseDto::from)
                .toList();
    }
}