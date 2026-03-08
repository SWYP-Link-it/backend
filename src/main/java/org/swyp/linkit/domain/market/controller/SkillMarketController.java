package org.swyp.linkit.domain.market.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.domain.market.dto.response.SkillCardPageResponseDto;
import org.swyp.linkit.domain.market.dto.response.SkillDetailDto;
import org.swyp.linkit.domain.market.dto.response.SkillSitemapResponseDto;
import org.swyp.linkit.domain.market.service.SkillMarketService;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.global.common.dto.ApiResponseDto;
import org.swyp.linkit.global.swagger.annotation.ApiErrorExceptionsExample;
import org.swyp.linkit.global.swagger.docs.SkillMarketExceptionDocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "SkillMarket", description = "스킬 장터 관련 API")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/market")
public class SkillMarketController {

    private final SkillMarketService skillMarketService;

    @Operation(
            summary = "스킬 카드 목록 조회",
            description = "스킬 장터 메인 페이지에 표시될 노출 중인 스킬 카드 목록을 조회합니다. "
            + "카테고리 및 검색 키워드 파라미터를 통해 필터링할 수 있습니다. "
            + "첫 요청은 cursorId 없이 size=11, 이후 요청은 응답의 nextCursorId 를 cursorId 로 전달해주세요."
    )
    @GetMapping(value = "/skills", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<SkillCardPageResponseDto>> getSkillCards(
            @Parameter(description = "스킬 카테고리 (선택)", example = "DEVELOPMENT")
            @RequestParam(required = false) SkillCategoryType category,

            @Parameter(description = "검색 키워드 (선택)", example = "React")
            @Size(max = 50) @RequestParam(required = false) String searchKeyword,

            @Parameter(description = "커서 ID, 첫 요청 시 생략", example = "10")
            @RequestParam(required = false) Long cursorId,
            
            @Parameter(description = "페이지 사이즈, 첫 요청 11 / 이후 요청 12", example = "11")
            @Min(1) @Max(12) @RequestParam(required = false, defaultValue = "11") int size) {
        log.info("[SkillMarket] GET /market/skills : category={}, searchKeyword={}, cursorId={}, size={}",
                category, searchKeyword, cursorId, size);

        SkillCardPageResponseDto response = skillMarketService
                .getVisibleSkills(category, searchKeyword, cursorId, size);

        return ResponseEntity.ok(
                ApiResponseDto.success("스킬 카드 목록을 조회했습니다.", response)
        );
    }

    @Operation(
            summary = "사이트맵용 스킬 목록 조회",
            description = "사이트맵 생성을 위한 노출 중인 스킬 목록을 조회합니다. skillId와 modifiedAt만 반환합니다."
    )
    @GetMapping(value = "/sitemap", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<List<SkillSitemapResponseDto>>> getSkillsForSitemap() {
        log.info("[SkillMarket] GET /market/sitemap");

        List<SkillSitemapResponseDto> skills = skillMarketService.getSkillsForSitemap();

        return ResponseEntity.ok(
                ApiResponseDto.success("사이트맵용 스킬 목록을 조회했습니다.", skills)
        );
    }

    @Operation(
            summary = "스킬 상세 정보 조회",
            description = "스킬 ID로 스킬 상세 정보와 프로필 정보를 함께 조회합니다. "
            + "해당 사용자의 다른 스킬 목록도 포함됩니다."
    )
    @ApiErrorExceptionsExample(SkillMarketExceptionDocs.GetSkillDetail.class)
    @GetMapping(value = "/skills/{skillId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<SkillDetailDto>> getSkillDetail(
            @Parameter(description = "스킬 ID", required = true)
            @PathVariable Long skillId) {
        log.info("[SkillMarket] GET /market/skills/{}", skillId);

        SkillDetailDto skillDetail = skillMarketService.getSkillDetail(skillId);

        return ResponseEntity.ok(
                ApiResponseDto.success("스킬 상세 정보를 조회했습니다.", skillDetail)
        );
    }
}
