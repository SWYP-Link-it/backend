package org.swyp.linkit.domain.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.market.dto.response.SkillCardResponseDto;
import org.swyp.linkit.domain.search.dto.response.PopularKeywordDto;
import org.swyp.linkit.domain.search.repository.projection.PopularKeywordView;
import org.swyp.linkit.domain.search.repository.SearchKeywordStatRepository;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.repository.UserSkillRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final UserSkillRepository userSkillRepository;
    private final SearchKeywordStatRepository searchKeywordStatRepository;

    // 스킬명으로 검색 (완전 일치) + 검색어 집계
    @Transactional
    public List<SkillCardResponseDto> searchSkills(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            log.warn("검색 키워드가 비어있습니다.");
            return List.of();
        }

        String trimmedKeyword = keyword.trim();

        // 검색어 집계 (UPSERT)
        recordSearchKeyword(trimmedKeyword);

        // 스킬 검색
        List<UserSkill> skills = userSkillRepository.searchVisibleSkillsByName(trimmedKeyword);

        log.info("스킬 검색 (완전 일치): keyword='{}', count={}", trimmedKeyword, skills.size());

        return skills.stream()
                .map(SkillCardResponseDto::from)
                .toList();
    }

    // 검색어 집계 (일별 카운트 증가)
    private void recordSearchKeyword(String keyword) {
        searchKeywordStatRepository.upsertIncrement(LocalDate.now(), keyword);
        log.debug("검색어 집계: keyword='{}', date={}", keyword, LocalDate.now());
    }

    // 최근 일주일 인기 검색어 Top 5
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
}