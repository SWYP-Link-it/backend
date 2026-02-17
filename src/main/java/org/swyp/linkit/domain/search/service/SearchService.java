package org.swyp.linkit.domain.search.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.market.dto.response.SkillCardPageResponseDto;
import org.swyp.linkit.domain.search.dto.response.PopularKeywordDto;
import org.swyp.linkit.domain.search.dto.response.PopularSkillDto;
import org.swyp.linkit.domain.search.repository.SearchKeywordStatRepository;
import org.swyp.linkit.domain.search.repository.SkillViewStatRepository;
import org.swyp.linkit.domain.search.repository.projection.PopularKeywordView;
import org.swyp.linkit.domain.search.repository.projection.PopularSkillView;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.repository.UserSkillRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private final UserSkillRepository userSkillRepository;
    private final SearchKeywordStatRepository searchKeywordStatRepository;
    private final SkillViewStatRepository skillViewStatRepository;
		private final SearchKeywordRecorder searchKeywordRecorder;

    /**
     * 스킬명으로 노출 중인 스킬 커서 기반 페이징 검색
		 * - category 가 null 이면 전체 카테고리 검색, 값이 있으면 해당 카테고리 내 검색
     * - keyword 가 null 이거나 비어있으면 빈 결과 반환
     * - 검색어 집계는 별도 트랜잭션으로 처리
     */
		@Transactional(readOnly = true)
    public SkillCardPageResponseDto searchSkills(SkillCategoryType category, String keyword,
                                                  Long cursorId, int size) {
        if (keyword == null || keyword.isBlank()) {
            log.warn("검색 키워드가 비어있습니다.");
            return SkillCardPageResponseDto.empty();
        }

        String trimmedKeyword = keyword.trim();

        // 검색어 집계 (별도 트랜잭션)
        searchKeywordRecorder.record(trimmedKeyword);

        // 커서 기반 페이징 검색
        Pageable pageable = PageRequest.of(0, size);
        Slice<UserSkill> slice = userSkillRepository
                .findVisibleSkillsWithCursor(category, trimmedKeyword, cursorId, pageable);

        log.info("스킬 검색: category={}, keyword='{}', cursorId={}, size={}, resultCount={}, hasNext={}",
                category, trimmedKeyword, cursorId, size,
                slice.getNumberOfElements(), slice.hasNext());

        return SkillCardPageResponseDto.of(slice);
    }

    // 최근 일주일 인기 검색어 Top 5
    @Transactional(readOnly = true)
    public List<PopularKeywordDto> getPopularKeywords() {
        LocalDate startDate = LocalDate.now().minusDays(6);  // 오늘 포함 7일
        List<PopularKeywordView> rows = searchKeywordStatRepository.findPopularKeywords(
                startDate,
                PageRequest.of(0, 5)
        );

        List<PopularKeywordDto> popularKeywords = IntStream.range(0, rows.size())
                .mapToObj(i -> PopularKeywordDto.of(i + 1, rows.get(i).getKeyword()))
                .toList();

        log.info("인기 검색어 Top 5 조회: count={}", popularKeywords.size());

        return popularKeywords;
    }

    // 최근 일주일 인기 스킬 Top 5
    @Transactional(readOnly = true)
    public List<PopularSkillDto> getPopularSkills() {
        LocalDate startDate = LocalDate.now().minusDays(6);  // 오늘 포함 7일
        List<PopularSkillView> views = skillViewStatRepository.findPopularSkills(
                startDate,
                PageRequest.of(0, 5)
        );

        // skillId로 UserSkill 조회
        List<Long> skillIds = views.stream()
                .map(PopularSkillView::getSkillId)
                .toList();

        List<UserSkill> skills = userSkillRepository.findAllById(skillIds);

        // skillId 순서대로 정렬 (Top 5 순서 유지)
        Map<Long, UserSkill> skillMap = skills.stream()
                .collect(Collectors.toMap(UserSkill::getId, skill -> skill));

        List<PopularSkillDto> popularSkills = views.stream()
                .map(view -> skillMap.get(view.getSkillId()))
                .filter(Objects::nonNull)
                .map(PopularSkillDto::from)
                .toList();

        log.info("인기 스킬 Top 5 조회: count={}", popularSkills.size());

        return popularSkills;
    }
}