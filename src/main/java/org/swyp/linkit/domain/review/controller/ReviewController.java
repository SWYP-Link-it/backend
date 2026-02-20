package org.swyp.linkit.domain.review.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.domain.review.dto.ReviewDto;
import org.swyp.linkit.domain.review.dto.request.ReviewRequestDto;
import org.swyp.linkit.domain.review.dto.request.ReviewUpdateRequestDto;
import org.swyp.linkit.domain.review.dto.response.ReceivedReviewRatingInfoResponseDto;
import org.swyp.linkit.domain.review.dto.response.ReviewDetailsResponseDto;
import org.swyp.linkit.domain.review.dto.response.ReviewResponseDto;
import org.swyp.linkit.domain.review.service.ReviewService;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.common.dto.ApiResponseDto;
import org.swyp.linkit.global.swagger.annotation.ApiErrorExceptionsExample;
import org.swyp.linkit.global.swagger.docs.ReviewExceptionDocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
@Tag(name = "Review", description = "리뷰 관련 API")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 작성
     */
    @Operation(
            summary = "리뷰 및 평점 작성 요청",
            description = "완료됨 상태의 스킬 거래에 대한 리뷰 및 평점 작성을 요청합니다."
    )
    @ApiErrorExceptionsExample(ReviewExceptionDocs.CreateReview.class)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<ReviewResponseDto>> createReview(
            @AuthenticationPrincipal CustomOAuth2User auth2User,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "리뷰 생성 정보")
            @RequestBody @Validated ReviewRequestDto requestDto) {

        ReviewResponseDto responseDto =
                reviewService.createReview(ReviewDto.ofCreate(auth2User.getUserId(), requestDto));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }

    /**
     * 리뷰 수정
     */
    @Operation(
            summary = "리뷰 및 평점 수정 요청",
            description = "완료됨 상태의 스킬 거래에 대한 리뷰 및 평점 작성을 요청합니다."
    )
    @ApiErrorExceptionsExample(ReviewExceptionDocs.UpdateReview.class)
    @PutMapping(value = "/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)

    public ResponseEntity<ApiResponseDto<ReviewResponseDto>> updateReview(
            @AuthenticationPrincipal CustomOAuth2User auth2User,

            @Parameter(description = "리뷰 ID", example = "1")
            @PathVariable Long reviewId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "리뷰 수정 정보")
            @RequestBody @Validated ReviewUpdateRequestDto requestDto) {

        ReviewResponseDto responseDto =
                reviewService.updateReview(ReviewDto.ofUpdate(auth2User.getUserId(),reviewId, requestDto));

        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }

    /**
     * 리뷰 삭제
     */
    @Operation(
            summary = "리뷰 및 평점 삭제 요청",
            description = "리뷰 및 평점을 삭제합니다."
    )
    @ApiErrorExceptionsExample(ReviewExceptionDocs.DeleteReview.class)
    @DeleteMapping(value = "/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<ReviewResponseDto>> deleteReview(
            @AuthenticationPrincipal CustomOAuth2User auth2User,

            @Parameter(description = "리뷰 ID", example = "1")
            @PathVariable Long reviewId) {

            reviewService.deleteReview(reviewId, auth2User.getUserId());

        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", null));
    }

    /**
     * 받은 리뷰 페이지 - 유저 전체 평점 및 스킬별 평점 탭 목록 조회
     */
    @Operation(
            summary = "받은 리뷰 평점 및 스킬 탭 목록 조회",
            description = "받은 리뷰 페이지 진입 시 유저 전체 평점과 스킬별 평점 탭 목록을 조회합니다."
    )
    @GetMapping(value = "/received/rating-info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<ReceivedReviewRatingInfoResponseDto>> getReceivedReviewRatingInfo(
            @AuthenticationPrincipal CustomOAuth2User auth2User) {

        ReceivedReviewRatingInfoResponseDto responseDto =
                reviewService.getReceivedReviewRatingInfo(auth2User.getUserId());

        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }

    /**
     * 리뷰 단건 조회
     */
    @Operation(summary = "리뷰 단건 조회", description = "리뷰를 조회합니다.")
    @ApiErrorExceptionsExample(ReviewExceptionDocs.GetReview.class)
    @GetMapping(value = "/{reviewId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<ReviewResponseDto>> getReview(
            @AuthenticationPrincipal CustomOAuth2User auth2User,

            @Parameter(description = "리뷰 ID", example = "1")
            @PathVariable Long reviewId){

        ReviewResponseDto responseDto = reviewService.getReview(auth2User.getUserId(), reviewId);
        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }

    /**
     * 유저가 받은 리뷰 스킬별 페이징 조회
     */
    @Operation(
            summary = "유저가 받은 리뷰 스킬별 내역 조회 - !리뷰 페이지에서 사용됩니다!",
            description = """
                유저가 가진 스킬별 리뷰를 조회합니다. 
                
                - 리뷰 페이지에서 받은 리뷰 조회 시 사용됩니다.
                - skillId를 전달하지 않을 경우 유저의 모든 스킬에 대한 리뷰를 응답합니다.
                """
    )
    @ApiErrorExceptionsExample(ReviewExceptionDocs.GetReceivedReviews.class)
    @GetMapping(value = "/received", produces = MediaType.APPLICATION_JSON_VALUE)

    public ResponseEntity<ApiResponseDto<ReviewDetailsResponseDto>> getReceivedReviews(
            @AuthenticationPrincipal CustomOAuth2User auth2User,

            @Parameter(description = "조회하고자 하는 유저의 스킬 ID (입력하지 않으면 전체 조회)", example = "3")
            @RequestParam(required = false) Long skillId,

            @Parameter(description = "다음 페이지 조회를 위한 커서 ID", example = "5")
            @RequestParam(required = false) Long nextCursor,

            @Parameter(description = "조회할 데이터 개수", example = "5")
            @RequestParam(required = false, defaultValue = "5") int size) {

        ReviewDetailsResponseDto responseDto = reviewService.getReceivedReviews(auth2User.getUserId(), skillId, nextCursor, size);
        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }

    /**
     * 유저가 작성한 리뷰 페이징 조회
     */
    @Operation(
            summary = "유저가 작성한 리뷰 내역 조회 - !리뷰 페이지에서 사용됩니다!",
            description = """
                유저가 작성한 리뷰를 조회합니다.
                
                - 리뷰 페이지에서 작성한 리뷰 조회 시 사용됩니다.
                - **reviewerNickname : 멘토의 닉네임 (응답 객체 재사용으로 인해 필드명이 reviewerNickname으로 표기됩니다.)**             
                """
    )
    @ApiErrorExceptionsExample(ReviewExceptionDocs.GetWrittenReviews.class)
    @GetMapping(value = "/written", produces = MediaType.APPLICATION_JSON_VALUE)

    public ResponseEntity<ApiResponseDto<ReviewDetailsResponseDto>> getWrittenReviews(
            @AuthenticationPrincipal CustomOAuth2User auth2User,

            @Parameter(description = "다음 페이지 조회를 위한 커서 ID", example = "5")
            @RequestParam(required = false) Long nextCursor,

            @Parameter(description = "조회할 데이터 개수", example = "5")
            @RequestParam(required = false, defaultValue = "5") int size) {

        ReviewDetailsResponseDto responseDto = reviewService.getWrittenReviews(auth2User.getUserId(), nextCursor, size);
        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }

    /**
     * 스킬별 리뷰 페이징 조회
     */
    @Operation(
            summary = "스킬별 리뷰 내역 조회 - !스킬 거래 장터 상세 조회 시 사용됩니다!",
            description = """
                스킬별 리뷰를 조회합니다.
                
                - 스킬 거래 장터 상세 조회 시 스킬별 리뷰 페이징 조회에 사용됩니다.
                """
    )
    @GetMapping(value = "skills/{skillId}", produces = MediaType.APPLICATION_JSON_VALUE)

    public ResponseEntity<ApiResponseDto<ReviewDetailsResponseDto>> getSkillReviews(
            @Parameter(description = "조회하고자 하는 유저의 스킬 ID", example = "3")
            @PathVariable(required = true) Long skillId,

            @Parameter(description = "다음 페이지 조회를 위한 커서 ID", example = "5")
            @RequestParam(required = false) Long nextCursor,

            @Parameter(description = "조회할 데이터 개수", example = "5")
            @RequestParam(required = false, defaultValue = "5") int size) {

        ReviewDetailsResponseDto responseDto = reviewService.getSkillReviews(skillId, nextCursor, size);
        return ResponseEntity.ok(ApiResponseDto.success("요청이 정상적으로 처리되었습니다.", responseDto));
    }
}
