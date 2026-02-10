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
import org.swyp.linkit.domain.exchange.dto.response.*;
import org.swyp.linkit.domain.exchange.service.SkillExchangeService;
import org.swyp.linkit.domain.user.dto.response.UserSkillForExchangeDto;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.common.dto.ApiResponseDto;
import org.swyp.linkit.global.swagger.annotation.ApiErrorExceptionsExample;
import org.swyp.linkit.global.swagger.docs.SkillExchangeExceptionDocs;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exchange")
@Tag(name = "Exchange", description = "스킬 거래 관련 API")
public class SkillExchangeController {

    private final SkillExchangeService exchangeService;

    /**
     * 멘토의 교환 가능 스킬 정보 조회
     */
    @Operation(
            summary = "멘토의 교환 가능 스킬 정보 조회",
            description = "거래 신청 전, 멘토가 제공 가능한 스킬 목록에 대한 정보를 조회합니다."
    )
    @ApiErrorExceptionsExample(SkillExchangeExceptionDocs.GetReceiverSkillDetails.class)
    @GetMapping(value = "/mentors/{mentorId}/skills", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<List<ReceiverSkillsResponseDto>>> getReceiverSkillDetails(
            @Parameter(description = "멘토의 사용자 ID", example = "1")
            @PathVariable Long mentorId) {

        List<ReceiverSkillsResponseDto> responseDto = exchangeService.getSkills(mentorId);
        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }

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

            @Parameter(description = "조회하고자 하는 멘토의 스킬 ID", example = "10")
            @RequestParam Long mentorSkillId,

            @Parameter(description = "조회할 년-월 (YYYY-MM)", example = "2026-01")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month){

        AvailableDatesResponseDto responseDto = exchangeService.getAvailableDates(mentorId, mentorSkillId, month.toString());
        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }

    /**
     *  멘토의 날짜 별 거래 가능 시간 조회
     */
    @Operation(
            summary = "멘토의 날짜 별 거래 가능 시간 조회",
            description = "날짜 별 멘토의 거래 가능 시간을 조회합니다."
    )
    @ApiErrorExceptionsExample(SkillExchangeExceptionDocs.GetAvailableSlots.class)
    @GetMapping(value = "/mentors/{mentorId}/available-slots", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<AvailableSlotsResponseDto>> getAvailableSlots(
            @Parameter(description = "멘토의 사용자 ID", example = "1")
            @PathVariable Long mentorId,

            @Parameter(description = "조회하고자 하는 멘토의 스킬 ID", example = "10")
            @RequestParam Long mentorSkillId,

            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2026-01-26")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){

        AvailableSlotsResponseDto responseDto = exchangeService.getAvailableSlots(mentorId, mentorSkillId, date);
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

    /**
     *  네비바 요청 관리 알림 및 요청 관리 진입 시 "받은 요청", "보낸 요청" 알림 용
     */
    @Operation(
            summary = "네비바 요청 관리 알림 및 요청 관리 진입 시 \"받은 요청\", \"보낸 요청\" 알림 조회",
            description = "네비바 요청 관리 알림 및 요청 관리 페이지 진입 시 구분되는 탭에 거래 상태가 변경되었는지 조회합니다."
    )
    @GetMapping("/request/notification")
    public ResponseEntity<ApiResponseDto<SkillExchangeNotificationResponseDto>> getNotification(
            @AuthenticationPrincipal CustomOAuth2User auth2User){

        SkillExchangeNotificationResponseDto responseDto = exchangeService.getNotification(auth2User.getUserId());
        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }

    /**
     *  스킬 거래 보낸 요청 내역 커서 기반 페이징 조회
     */
    @Operation(
            summary = "스킬 거래 보낸 요청 내역 조회",
            description = "스킬 거래 보낸 요청 내역을 조회합니다."
    )
    @ApiErrorExceptionsExample(SkillExchangeExceptionDocs.Paging.class)
    @GetMapping(value = "/request/sent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<SkillExchangeDetailsResponseDto>> getSentRequests(
            @AuthenticationPrincipal CustomOAuth2User auth2User,
            @Parameter(description = "다음 페이지 조회를 위한 커서 ID", example = "5")
            @RequestParam(required = false) Long nextCursor,
            @Parameter(description = "조회할 데이터 개수", example = "5")
            @RequestParam(required = false, defaultValue = "5") int size){

        SkillExchangeDetailsResponseDto responseDto = exchangeService.getSentRequests(auth2User.getUserId(), nextCursor, size);
        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }

    /**
     *  스킬 거래 받은 요청 내역 커서 기반 페이징 조회
     */
    @Operation(
            summary = "스킬 거래 받은 요청 내역 조회",
            description = "스킬 거래 받은 요청 내역을 조회합니다."
    )
    @ApiErrorExceptionsExample(SkillExchangeExceptionDocs.Paging.class)
    @GetMapping(value = "/request/received", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<SkillExchangeDetailsResponseDto>> getReceived(
            @AuthenticationPrincipal CustomOAuth2User auth2User,
            @Parameter(description = "다음 페이지 조회를 위한 커서 ID", example = "5")
            @RequestParam(required = false) Long nextCursor,
            @Parameter(description = "조회할 데이터 개수", example = "5")
            @RequestParam(required = false, defaultValue = "5") int size){

        SkillExchangeDetailsResponseDto responseDto = exchangeService.getReceivedRequests(auth2User.getUserId(), nextCursor, size);
        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }

    /**
     *  스킬 거래 수락
     */
    @Operation(
            summary = "스킬 거래 수락 요청",
            description = "멘토가 스킬 거래를 수락 합니다. 수락은 대기중 상태만 가능합니다."
    )
    @ApiErrorExceptionsExample(SkillExchangeExceptionDocs.AcceptSkillExchange.class)
    @PostMapping(value = "/request/{skillExchangeId}/accept", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<SkillExchangeResponseDto>> acceptSkillExchange(
            @AuthenticationPrincipal CustomOAuth2User auth2User,
            @Parameter(description = "스킬 거래의 ID", example = "1")
            @PathVariable Long skillExchangeId){

        SkillExchangeResponseDto responseDto = exchangeService
                .acceptSkillExchange(auth2User.getUserId(), skillExchangeId);
        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }

    /**
     *  스킬 거래 거절
     */
    @Operation(
            summary = "스킬 거래 거절 요청",
            description = "멘토가 스킬 거래를 거절 합니다. 거절은 대기중 상태만 가능합니다."
    )
    @ApiErrorExceptionsExample(SkillExchangeExceptionDocs.RejectSkillExchange.class)
    @PostMapping(value = "/request/{skillExchangeId}/reject", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<SkillExchangeResponseDto>> rejectSkillExchange(
            @AuthenticationPrincipal CustomOAuth2User auth2User,
            @Parameter(description = "스킬 거래의 ID", example = "1")
            @PathVariable Long skillExchangeId){

        SkillExchangeResponseDto responseDto = exchangeService
                .rejectSkillExchange(auth2User.getUserId(), skillExchangeId);
        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }

    /**
     *  스킬 거래 취소
     */
    @Operation(
            summary = "스킬 거래 취소 요청",
            description = "스킬 거래를 취소 합니다. (멘토 : 수락됨, 멘티 : 대기중, 수락됨) 상태에서만 취소가 가능합니다."
    )
    @ApiErrorExceptionsExample(SkillExchangeExceptionDocs.CancelSkillExchange.class)
    @PostMapping(value = "/request/{skillExchangeId}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<SkillExchangeResponseDto>> cancelSkillExchange(
            @AuthenticationPrincipal CustomOAuth2User auth2User,
            @Parameter(description = "스킬 거래의 ID", example = "1")
            @PathVariable Long skillExchangeId){

        SkillExchangeResponseDto responseDto = exchangeService
                .cancelSkillExchange(auth2User.getUserId(), skillExchangeId);
        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }
}