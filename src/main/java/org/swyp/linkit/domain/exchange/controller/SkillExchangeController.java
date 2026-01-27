package org.swyp.linkit.domain.exchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.swyp.linkit.domain.exchange.dto.SkillExchangeDto;
import org.swyp.linkit.domain.exchange.dto.request.SkillExchangeRequestDto;
import org.swyp.linkit.domain.exchange.dto.response.AvailableDatesResponseDto;
import org.swyp.linkit.domain.exchange.dto.response.AvailableSlotsResponseDto;
import org.swyp.linkit.domain.exchange.dto.response.SkillExchangeResponseDto;
import org.swyp.linkit.domain.exchange.service.SkillExchangeService;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.common.dto.ApiResponseDto;
import org.swyp.linkit.global.swagger.annotation.ApiErrorExceptionsExample;
import org.swyp.linkit.global.swagger.docs.SkillExchangeExceptionDocs;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exchange")
@Tag(name = "Exchange", description = "스킬 거래 관련 API")
public class SkillExchangeController {

    private final SkillExchangeService exchangeService;

    /**
     *  멘토의 거래 가능 날짜 조회
     */
    @Operation(
            summary = "멘토의 월별 거래 가능 날짜 조회",
            description = "월별 멘토의 거래 가능 날짜를 조회합니다."
    )
    @ApiErrorExceptionsExample(SkillExchangeExceptionDocs.GetAvailableDates.class)
    @GetMapping(value = "/mentors/{mentorId}/available-dates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<AvailableDatesResponseDto>> getAvailableDates(
            @Parameter(description = "멘토의 사용자 ID", example = "1")
            @PathVariable Long mentorId,

            @Parameter(description = "조회할 년-월 (YYYY-MM)", example = "2026-01")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month){

        AvailableDatesResponseDto responseDto = exchangeService.getAvailableDates(mentorId, month.toString());
        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }

    /**
     *  멘토의 날짜 별 거래 가능 시간 조회
     */
    @Operation(
            summary = "멘토의 날짜 별 거래 가능 시간 조회",
            description = "날짜 별 멘토의 거래 가능 시간을 조회합니다. 시간은 30분 단위로 응답합니다."
    )
    @ApiErrorExceptionsExample(SkillExchangeExceptionDocs.GetAvailableSlots.class)
    @GetMapping(value = "/mentors/{mentorId}/available-slots", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<AvailableSlotsResponseDto>> getAvailableSlots(
            @Parameter(description = "멘토의 사용자 ID", example = "1")
            @PathVariable Long mentorId,

            @Parameter(description = "조회하고자 하는 멘토의 스킬 ID", example = "10")
            @RequestParam Long skillId,

            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2026-01-26")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){

        AvailableSlotsResponseDto responseDto = exchangeService.getAvailableSlots(mentorId, skillId, date);
        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }

    /**
     *  스킬 거래 요청
     */
    @Operation(
            summary = "스킬 거래 요청",
            description = "스킬 거래를 요청합니다."
    )
    @ApiErrorExceptionsExample(SkillExchangeExceptionDocs.CreateExchange.class)
    @PostMapping(value = "/request", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<SkillExchangeResponseDto>> createExchange(
            @AuthenticationPrincipal CustomOAuth2User auth2User,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "스킬 교환 요청 정보")
            @RequestBody @Validated SkillExchangeRequestDto requestDto){

        SkillExchangeResponseDto responseDto = exchangeService
                .requestSkillExchange(auth2User.getUserId(), SkillExchangeDto.from(requestDto));
        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }
}
