package org.swyp.linkit.domain.market.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.domain.market.dto.response.SkillCardResponseDto;
import org.swyp.linkit.domain.market.dto.response.SkillDetailDto;
import org.swyp.linkit.domain.market.service.SkillMarketService;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.global.common.dto.ApiResponseDto;
import org.swyp.linkit.global.swagger.annotation.ApiErrorExceptionsExample;
import org.swyp.linkit.global.swagger.docs.SkillMarketExceptionDocs;

import java.util.List;

@Tag(name = "SkillMarket", description = "스킬 장터 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/market")
public class SkillMarketController {

    private final SkillMarketService skillMarketService;

    @Operation(
            summary = "스킬 카드 목록 조회",
            description = "스킬 장터 메인 페이지에 표시될 노출 중인 스킬 카드 목록을 최신순으로 조회합니다. " +
                    "카테고리 파라미터를 통해 특정 카테고리의 스킬만 조회할 수 있습니다."
    )
    @GetMapping(value = "/skills", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<List<SkillCardResponseDto>>> getSkillCards(
            @Parameter(description = "스킬 카테고리 (선택)", example = "DEVELOPMENT")
            @RequestParam(required = false) SkillCategoryType category) {

        log.info("[SkillMarket] GET /market/skills : category={}", category);

        List<SkillCardResponseDto> skills = skillMarketService.getVisibleSkills(category);

        return ResponseEntity.ok(
                ApiResponseDto.success("스킬 카드 목록을 조회했습니다.", skills)
        );
    }

    @Operation(
            summary = "스킬 상세 정보 조회",
            description = "스킬 ID로 스킬 상세 정보와 프로필 정보를 함께 조회합니다. " +
                    "해당 사용자의 다른 스킬 목록도 포함됩니다."
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