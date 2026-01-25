package org.swyp.linkit.domain.credit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.domain.credit.dto.CreditDto;
import org.swyp.linkit.domain.credit.dto.CreditWithUserDetailsDto;
import org.swyp.linkit.domain.credit.dto.response.CreditBalanceResponseDto;
import org.swyp.linkit.domain.credit.dto.response.CreditBalanceWithUserDetailsResponseDto;
import org.swyp.linkit.domain.credit.dto.response.CreditHistoryResponseDto;
import org.swyp.linkit.domain.credit.entity.SupplyType;
import org.swyp.linkit.domain.credit.service.CreditHistoryService;
import org.swyp.linkit.domain.credit.service.CreditService;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.common.dto.ApiResponseDto;
import org.swyp.linkit.global.swagger.annotation.ApiErrorExceptionsExample;
import org.swyp.linkit.global.swagger.docs.CreditExceptionDocs;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/credits")
@Tag(name = "Credit", description = "크레딧 관련 API")
public class CreditController {

    private final CreditService creditService;
    private final CreditHistoryService historyService;

    /**
     *  크레딧 잔액 조회
     */
    @Operation(summary = "사용자의 크레딧 잔액 조회", description = "사용자의 크레딧 잔액만을 조회합니다.")
    @ApiErrorExceptionsExample(CreditExceptionDocs.GetCreditBalance.class)
    @GetMapping(value = "/balance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<CreditBalanceResponseDto>> getCreditBalance(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        log.info("[Credit] Get getCreditBalance : userId= {}", oAuth2User.getUserId());
        CreditDto creditDto = creditService.getCreditBalance(oAuth2User.getUserId());
        CreditBalanceResponseDto responseDto = CreditBalanceResponseDto.from(creditDto);

        return ResponseEntity.ok().body(ApiResponseDto.success("조회 성공", responseDto));
    }

    /**
     *  크레딧 잔액 및 유저 정보 조회
     */
    @Operation(summary = "사용자의 크레딧 잔액 및 유저 정보 조회", description = "사용자의 크레딧 잔액과 유저의 정보를 조회합니다.")
    @ApiErrorExceptionsExample(CreditExceptionDocs.GetCreditBalanceWithUserDetails.class)
    @GetMapping(value = "/balance-user-details", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<CreditBalanceWithUserDetailsResponseDto>> getCreditBalanceWithUserDetails(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        log.info("[Credit] Get getCreditBalanceWithUserDetails : userId= {}", oAuth2User.getUserId());
        CreditWithUserDetailsDto cd = creditService.getCreditBalanceWithUserDetails(oAuth2User.getUserId());
        CreditBalanceWithUserDetailsResponseDto responseDto = CreditBalanceWithUserDetailsResponseDto.from(cd);

        return ResponseEntity.ok().body(ApiResponseDto.success("조회 성공", responseDto));
    }

    /**
     *  크레딧 내역 커서 기반 페이징 조회
     */
    @Operation(summary = "사용자의 크레딧 내역 조회", description = "사용자의 크레딧 내역을 조회합니다.")
    @ApiErrorExceptionsExample(CreditExceptionDocs.GetCreditHistories.class)
    @GetMapping(value = "/histories", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<CreditHistoryResponseDto>> getCreditHistories(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @RequestParam(required = false) Long nextCursor,
            @RequestParam(required = false) SupplyType supplyType,
            @RequestParam(required = false, defaultValue = "5") int size) {

        log.info("[Credit] Get getCreditHistories : userId= {}", oAuth2User.getUserId());
        CreditHistoryResponseDto responseDto = historyService
                .getUserCreditHistories(oAuth2User.getUserId(), supplyType, nextCursor, size);

        return ResponseEntity.ok().body(ApiResponseDto.success("조회 성공", responseDto));
    }
}
