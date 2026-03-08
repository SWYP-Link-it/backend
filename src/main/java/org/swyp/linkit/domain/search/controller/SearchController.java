package org.swyp.linkit.domain.search.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.domain.search.dto.response.PopularKeywordDto;
import org.swyp.linkit.domain.search.dto.response.PopularSkillDto;
import org.swyp.linkit.domain.search.service.SearchRankingService;
import org.swyp.linkit.global.common.dto.ApiResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Search", description = "검색 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchRankingService searchRankingService;

    @Operation(
            summary = "인기 검색어 Top 5",
            description = "최근 일주일 기준 인기 검색어 Top 5를 조회합니다."
    )
    @GetMapping(value = "/keywords/popular", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<List<PopularKeywordDto>>> getPopularKeywords() {

        log.info("[Search] GET /search/keywords/popular");

        List<PopularKeywordDto> popularKeywords = searchRankingService.getPopularKeywords();

        return ResponseEntity.ok(
                ApiResponseDto.success("인기 검색어 Top 5를 조회했습니다.", popularKeywords)
        );
    }

    @Operation(
            summary = "인기 스킬 Top 5",
            description = "최근 일주일 기준 조회수가 높은 인기 스킬 Top 5를 조회합니다."
    )
    @GetMapping(value = "/skills/popular", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<List<PopularSkillDto>>> getPopularSkills() {

        log.info("[Search] GET /search/skills/popular");

        List<PopularSkillDto> popularSkills = searchRankingService.getPopularSkills();

        return ResponseEntity.ok(
                ApiResponseDto.success("인기 스킬 Top 5를 조회했습니다.", popularSkills)
        );
    }
}
