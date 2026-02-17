package org.swyp.linkit.domain.search.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.domain.market.dto.response.SkillCardPageResponseDto;
import org.swyp.linkit.domain.search.dto.response.PopularKeywordDto;
import org.swyp.linkit.domain.search.dto.response.PopularSkillDto;
import org.swyp.linkit.domain.search.service.SearchRankingService;
import org.swyp.linkit.domain.search.service.SearchService;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.global.common.dto.ApiResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Search", description = "검색 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;
		private final SearchRankingService searchRankingService;

		@Operation(
						summary = "스킬 검색",
						description = "스킬명으로 노출 중인 스킬을 검색합니다. " +
										"첫 요청은 cursorId 없이, 이후 요청은 응답의 nextCursorId 를 cursorId 로 전달해주세요."
		)
		@GetMapping(value = "/skills", produces = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity<ApiResponseDto<SkillCardPageResponseDto>> searchSkills(
						@Parameter(description = "스킬 카테고리 (선택)", example = "DEVELOPMENT")
						@RequestParam(required = false) SkillCategoryType category,

						@Parameter(description = "검색 키워드", required = true, example = "React")
						@RequestParam String keyword,

						@Parameter(description = "커서 ID, 첫 요청 시 생략", example = "10")
						@RequestParam(required = false) Long cursorId,

						@Parameter(description = "페이지 사이즈", example = "12")
						@RequestParam(required = false, defaultValue = "12") int size) {

				log.info("[Search] GET /search/skills : keyword={}, category={}, cursorId={}, size={}",
								keyword, category, cursorId, size);

				SkillCardPageResponseDto response = searchService
								.searchSkills(category, keyword, cursorId, size);

				return ResponseEntity.ok(
								ApiResponseDto.success("스킬 검색 결과를 조회했습니다.", response)
				);
		}

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