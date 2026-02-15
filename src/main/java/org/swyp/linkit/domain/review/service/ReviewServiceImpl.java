package org.swyp.linkit.domain.review.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.exchange.service.SkillExchangeService;
import org.swyp.linkit.domain.review.dto.ReviewDto;
import org.swyp.linkit.domain.review.dto.UserSkillRatingStatDto;
import org.swyp.linkit.domain.review.dto.response.ReceivedReviewSkillDto;
import org.swyp.linkit.domain.review.dto.response.ReceivedReviewSkillsResponseDto;
import org.swyp.linkit.domain.review.dto.response.ReviewDetailsResponseDto;
import org.swyp.linkit.domain.review.dto.response.ReviewResponseDto;
import org.swyp.linkit.domain.review.entity.Review;
import org.swyp.linkit.domain.review.repository.ReviewRepository;
import org.swyp.linkit.domain.review.service.projection.ReceivedReviewSkillQuery;
import org.swyp.linkit.domain.review.service.projection.ReviewDetailQuery;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.global.error.exception.ReviewAccessDeniedException;
import org.swyp.linkit.global.error.exception.ReviewAlreadyExistsException;
import org.swyp.linkit.global.error.exception.ReviewInvalidStatusException;
import org.swyp.linkit.global.error.exception.ReviewNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final SkillExchangeService skillExchangeService;
    private final UserRatingStatService userRatingService;
    private final UserSkillRatingStatService userSkillRatingService;

    /**
     * 리뷰 작성
     */
    @Transactional
    @Override
    public ReviewResponseDto createReview(ReviewDto dto) {
        Long reviewerId = dto.getReviewerId();
        Long skillExchangeId = dto.getSkillExchangeId();

        // 1. 스킬 거래 조회 및 존재 여부 검증 -> ExchangeNotFoundException
        SkillExchange skillExchange = skillExchangeService.getById(skillExchangeId);

        // 2. 리뷰 작성 권한 검증 -> ExchangeReviewAccessDeniedException
        validateReviewAuthority(skillExchange.getRequester(), reviewerId, skillExchangeId);

        // 3. 스킬 거래 상태(COMPLETED) 검증 -> InvalidExchangeStatusException
        validateReviewableStatus(skillExchange.getExchangeStatus(), reviewerId, skillExchangeId);

        // 4. 리뷰 중복 작성 검증 -> ReviewAlreadyExistsException
        validateReviewDuplication(skillExchangeId, reviewerId);

        // 5. 리뷰 생성 및 저장
        Review savedReview = createReviewAndSave(dto, skillExchangeId, reviewerId);

        // 6. 유저 평점 집계 update
        userRatingService.updateUserRating(dto.getRevieweeId(), dto.getRating());

        // 7. 유저 스킬 평점 집계 update
        userSkillRatingService.updateUserSkillRating(dto.getRevieweeSkillId(), dto.getRating());

        // 8. 응답 Dto 변환
        return ReviewResponseDto.from(savedReview);
    }

    /**
     *  리뷰, 평점 수정
     */
    @Transactional
    @Override
    public ReviewResponseDto updateReview(ReviewDto dto) {
        Long reviewId = dto.getId();
        Long reviewerId = dto.getReviewerId();

        // 1. 리뷰 조회 및 존재 여부 검증
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("reviewId= " + reviewId));

        // 2. 리뷰 수정 권한 검증 -> ReviewAccessDeniedException
        validateUpdateAuthority(review, reviewerId);

        // 3. 평점 수정된 경우 집계 테이블 update
        if(!review.getRating().equals(dto.getRating())){
            changeRatingStatistics(review, dto.getRating());
        }

        // 4. 리뷰 수정
        review.update(dto.getContent(), dto.getRating());

        // 5. 응답 Dto 변환
        return ReviewResponseDto.from(review);
    }

    /**
     *  리뷰 삭제
     */
    @Transactional
    @Override
    public void deleteReview(Long reviewId, Long reviewerId) {
        // 1. 리뷰 조회 및 존재 여부 검증
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("reviewId= " + reviewId));

        // 2. 리뷰 삭제 권한 검증 -> ReviewAccessDeniedException
        validateUpdateAuthority(review, reviewerId);

        // 3. 평점 집계 감소
        userRatingService.decreaseUserRating(review.getRevieweeId(), review.getRating());
        userSkillRatingService.decreaseUserSkillRating(review.getRevieweeSkillId(), review.getRating());

        // 4. 리뷰 삭제 처리
        reviewRepository.delete(review);
    }

    /**
     * 유저가 받은 리뷰 스킬별 페이징 조회
     * skillId = Null 이면 모든 스킬에 대한 리뷰 조회
     */
    @Transactional(readOnly = true)
    @Override
    public ReviewDetailsResponseDto getReceivedReviews(Long userId, Long skillId, Long cursorId, int size) {
        // 1. Pageable 객체 생성
        Pageable pageable = PageRequest.of(0, size);

        // 2. 받은 리뷰 커서 기반 페이징 조회
        Slice<ReviewDetailQuery> slice = reviewRepository.
                findAllByRevieweeId(userId, skillId, cursorId, pageable);

        // 3. 평균 평점 조회
        Double avgRating;
        if (skillId == null) {
            // 전체 탭: 사용자 전체 평균 평점
            avgRating = userRatingService.getUserRating(userId).getAvgRating();
        } else {
            // 스킬 탭: 해당 스킬의 평균 평점
            avgRating = userSkillRatingService.getUserSkillRating(skillId).getAvgRating();
        }

        // 4. 응답 Dto 변환
        return ReviewDetailsResponseDto.from(slice, avgRating);
    }

    /**
     * 유저가 작성한 리뷰 페이징 조회
     */
    @Transactional(readOnly = true)
    @Override
    public ReviewDetailsResponseDto getWrittenReviews(Long userId, Long cursorId, int size) {
        // 1. Pageable 객체 생성
        Pageable pageable = PageRequest.of(0, size);

        // 2. 받은 리뷰 커서 기반 페이징 조회
        Slice<ReviewDetailQuery> slice = reviewRepository.
                findAllByReviewerId(userId, cursorId, pageable);

        // 3. 응답 Dto 변환
        return ReviewDetailsResponseDto.from(slice);
    }

    /**
     * 스킬 별 리뷰 페이징 조회
     */
    @Transactional(readOnly = true)
    @Override
    public ReviewDetailsResponseDto getSkillReviews(Long skillId, Long cursorId, int size) {
        // 1. Pageable 객체 생성
        Pageable pageable = PageRequest.of(0, size);

        // 2. 받은 리뷰 커서 기반 페이징 조회
        Slice<ReviewDetailQuery> slice = reviewRepository.
                findAllBySkillId(skillId, cursorId, pageable);

        // 3. 응답 Dto 변환
        return ReviewDetailsResponseDto.from(slice);
    }

    /**
     * 유저가 받은 리뷰의 스킬 목록 및 스킬별 평점 조회
     */
    @Transactional(readOnly = true)
    @Override
    public ReceivedReviewSkillsResponseDto getReceivedReviewSkills(Long userId) {
        // 1. 해당 유저가 받은 리뷰가 있는 스킬 목록 조회
        List<ReceivedReviewSkillQuery> skillQueries = reviewRepository.findReceivedReviewSkillsByUserId(userId);
        
        // 2. 각 스킬의 평점 정보 조회 및 DTO 변환
        List<ReceivedReviewSkillDto> skills = skillQueries.stream()
                .map(query -> {
                    // 스킬별 평점 조회
                    UserSkillRatingStatDto ratingDto = userSkillRatingService.getUserSkillRating(query.skillId());
                    
                    return ReceivedReviewSkillDto.of(
                            query.skillId(),
                            query.skillName(),
                            ratingDto.getAvgRating(),
                            ratingDto.getRatingCount()
                    );
                })
                .toList();
        
        // 3. 응답 DTO 변환
        return ReceivedReviewSkillsResponseDto.from(skills);
    }

    // == private Methods ==

    /**
     * 평점 통계 업데이트 (기존 평점 취소 후 새 평점 반영)
     */
    private void changeRatingStatistics(Review review, Integer newRating) {
        Integer oldRating = review.getRating();
        Long revieweeId = review.getRevieweeId();
        Long revieweeSkillId = review.getRevieweeSkillId();

        // 평점 수정 처리
        userRatingService.changeUserRating(revieweeId, oldRating, newRating);
        userSkillRatingService.changeUserSkillRating(revieweeSkillId, oldRating, newRating);
    }

    /**
     *  리뷰 생성 및 저장
     */
    private Review createReviewAndSave(ReviewDto dto, Long skillExchangeId, Long reviewerId) {
        Review review = Review.create(
                skillExchangeId,
                dto.getRevieweeSkillId(),
                reviewerId,
                dto.getRevieweeId(),
                dto.getContent(),
                dto.getRating()
        );
        return reviewRepository.save(review);
    }

    /**
     *  리뷰 중복 작성 검증
     */
    private void validateReviewDuplication(Long skillExchangeId, Long reviewerId) {
        if (reviewRepository.existsBySkillExchangeIdAndReviewerId(skillExchangeId, reviewerId)) {
            log.warn("이미 리뷰가 존재합니다. reviewerId= {}, skillExchangeId= {}", reviewerId, skillExchangeId);
            throw new ReviewAlreadyExistsException("reviewerId= " + reviewerId + ", skillExchangeId= " + skillExchangeId);
        }
    }

    /**
     *  스킬 거래 상태(COMPLETED) 검증
     */
    private void validateReviewableStatus(ExchangeStatus exchangeStatus, Long reviewerId, Long skillExchangeId) {
        if (exchangeStatus != ExchangeStatus.COMPLETED) {
            log.warn("거래가 완료된 스킬 거래에만 리뷰를 작성할 수 있습니다. reviewerId= {}, skillExchangeId= {}", reviewerId, skillExchangeId);
            throw new ReviewInvalidStatusException("reviewerId= " + reviewerId + ", skillExchangeId= " + skillExchangeId);
        }
    }

    /**
     *  리뷰 작성 권한 검증
     */
    private void validateReviewAuthority(User requester, Long reviewerId, Long skillExchangeId) {
        if (!requester.getId().equals(reviewerId)) {
            log.warn("리뷰 작성 권한이 없습니다. reviewerId= {}, skillExchangeId= {}", reviewerId, skillExchangeId);
            throw new ReviewAccessDeniedException("reviewerId= " + reviewerId + ", skillExchangeId= " + skillExchangeId);
        }
    }

    /**
     * 리뷰 수정 권한 검증
     */
    private void validateUpdateAuthority(Review review, Long reviewerId) {
        if (!review.getReviewerId().equals(reviewerId)) {
            log.warn("리뷰 수정 권한이 없습니다. reviewerId= {}, reviewId= {}", reviewerId, review.getId());
            throw new ReviewAccessDeniedException("reviewerId= " + reviewerId + ", reviewId= " + review.getId());
        }
    }
}
