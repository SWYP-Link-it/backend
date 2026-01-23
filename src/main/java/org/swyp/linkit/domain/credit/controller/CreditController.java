package org.swyp.linkit.domain.credit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.domain.credit.dto.CreditDto;
import org.swyp.linkit.domain.credit.dto.CreditWithUserDetailsDto;
import org.swyp.linkit.domain.credit.dto.response.CreditBalanceResponseDto;
import org.swyp.linkit.domain.credit.dto.response.CreditBalanceWithUserDetailsResponseDto;
import org.swyp.linkit.domain.credit.service.CreditService;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.common.dto.ApiResponseDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/credits")
@Tag(name = "Credit", description = "크레딧 관련 API")
public class CreditController {

    private final CreditService creditService;

    /**
     *  크레딧 잔액 조회
     */
    @Operation(summary = "사용자의 크레딧 잔액 조회", description = "사용자의 크레딧 잔액만을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "크레딧 정보를 찾을 수 없습니다.")})
    @GetMapping(value = "/balance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<CreditBalanceResponseDto>> getCreditBalance(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        CreditDto creditDto = creditService.getCreditBalance(oAuth2User.getUserId());
        CreditBalanceResponseDto responseDto = CreditBalanceResponseDto.from(creditDto);

        return ResponseEntity.ok().body(ApiResponseDto.success("조회 성공", responseDto));
    }

    /**
     *  크레딧 잔액 및 유저 정보 조회
     */
    @Operation(summary = "사용자의 크레딧 잔액 및 유저 정보 조회", description = "사용자의 크레딧 잔액과 유저의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "크레딧 정보를 찾을 수 없습니다.")})
    @GetMapping(value = "/balance-userdetails", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<CreditBalanceWithUserDetailsResponseDto>> getCreditBalanceWithUserDetails(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        CreditWithUserDetailsDto cd = creditService.getCreditBalanceWithUserDetails(oAuth2User.getUserId());
        CreditBalanceWithUserDetailsResponseDto responseDto = CreditBalanceWithUserDetailsResponseDto.from(cd);

        return ResponseEntity.ok().body(ApiResponseDto.success("조회 성공", responseDto));
    }

}