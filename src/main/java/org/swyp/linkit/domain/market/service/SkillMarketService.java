package org.swyp.linkit.domain.market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.market.dto.response.SkillCardResponseDto;
import org.swyp.linkit.domain.market.dto.response.SkillDetailDto;
import org.swyp.linkit.domain.review.dto.UserRatingStatDto;
import org.swyp.linkit.domain.review.dto.UserSkillRatingStatDto;
import org.swyp.linkit.domain.review.service.UserRatingStatService;
import org.swyp.linkit.domain.review.service.UserSkillRatingStatService;
import org.swyp.linkit.domain.search.repository.SearchKeywordStatRepository;
import org.swyp.linkit.domain.search.service.SearchService;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.repository.UserSkillRepository;
import org.swyp.linkit.global.error.exception.UserSkillNotFoundException;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillMarketService {

    private final UserSkillRepository userSkillRepository;
    private final SearchService searchService;
    private final SearchKeywordStatRepository searchKeywordStatRepository;
    private final UserRatingStatService userRatingStatService;
    private final UserSkillRatingStatService userSkillRatingStatService;

    // 노출 중인 스킬 카드 조회 (최신순)
    @Transactional
    public List<SkillCardResponseDto> getVisibleSkills(SkillCategoryType category, String searchKeyword) {
        List<UserSkill> skills;

        // 1. 카테고리 + 키워드 둘 다 있는 경우
        if (category != null && searchKeyword != null && !searchKeyword.isBlank()) {
            String trimmedKeyword = searchKeyword.trim();

            // 검색어 집계 추가
            recordSearchKeyword(trimmedKeyword);

            skills = userSkillRepository.findVisibleSkillsByCategoryAndKeyword(
                    category,
                    trimmedKeyword
            );
            log.info("카테고리+키워드 필터 조회: category={}, keyword={}, count={}",
                    category.getDescription(), trimmedKeyword, skills.size());
        }
        // 2. 카테고리만 있는 경우
        else if (category != null) {
            skills = userSkillRepository.findVisibleSkillsByCategory(category);
            log.info("카테고리별 스킬 조회: category={}, count={}",
                    category.getDescription(), skills.size());
        }
        // 3. 키워드만 있는 경우
        else if (searchKeyword != null && !searchKeyword.isBlank()) {
            String trimmedKeyword = searchKeyword.trim();

            // 검색어 집계 추가
            recordSearchKeyword(trimmedKeyword);

            skills = userSkillRepository.searchVisibleSkillsByName(trimmedKeyword);
            log.info("키워드 검색 (완전 일치): keyword={}, count={}", trimmedKeyword, skills.size());
        }
        // 4. 둘 다 없는 경우 (전체 조회)
        else {
            skills = userSkillRepository.findAllVisibleSkills();
            log.info("전체 스킬 조회: count={}", skills.size());
        }

        return skills.stream()
                .map(SkillCardResponseDto::from)
                .toList();
    }

    // 검색어 집계 (일별 카운트 증가)
    private void recordSearchKeyword(String keyword) {
        searchKeywordStatRepository.upsertIncrement(LocalDate.now(), keyword);
        log.debug("검색어 집계 (장터): keyword='{}', date={}", keyword, LocalDate.now());
    }

    // 스킬 ID로 상세 정보 조회 (스킬 + 프로필 전체 + 유저 평점 + 스킬별 평점)
    @Transactional
    public SkillDetailDto getSkillDetail(Long skillId) {
        // 1. 메인 스킬 조회 (이미지 포함)
        UserSkill mainSkill = userSkillRepository.findVisibleSkillDetailById(skillId)
                .orElseThrow(() -> new UserSkillNotFoundException("해당 스킬을 찾을 수 없거나 현재 노출 중이 아닙니다."));

        // 2. 조회수 집계
        searchService.recordSkillView(skillId);

        // 3. 해당 사용자의 모든 스킬 조회 (이미지 포함)
        Long userId = mainSkill.getOwnerId();
        List<UserSkill> allUserSkills = userSkillRepository.findVisibleSkillsWithImagesByUserId(userId);

        // 4. 유저 평점 조회
        UserRatingStatDto userRating = userRatingStatService.getUserRating(userId);

        // 5. 메인 스킬의 평점 조회
        UserSkillRatingStatDto mainSkillRating = userSkillRatingStatService.getUserSkillRating(skillId);

        log.info("스킬 상세 정보 조회: skillId={}, userId={}, totalSkillsCount={}, userAvgRating={}, skillAvgRating={}",
                skillId, userId, allUserSkills.size(), userRating.getAvgRating(), mainSkillRating.getAvgRating());

        // 6. DTO 변환
        return SkillDetailDto.from(mainSkill, allUserSkills, userRating, mainSkillRating);
    }
}