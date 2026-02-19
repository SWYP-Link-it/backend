//package org.swyp.linkit.domain.review.service;
//
//import jakarta.persistence.EntityManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//import org.swyp.linkit.TestRedisConfig;
//import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
//import org.swyp.linkit.domain.exchange.entity.SkillExchange;
//import org.swyp.linkit.domain.exchange.repository.SkillExchangeRepository;
//import org.swyp.linkit.domain.review.dto.ReviewDto;
//import org.swyp.linkit.domain.review.dto.request.ReviewRequestDto;
//import org.swyp.linkit.domain.review.dto.request.ReviewUpdateRequestDto;
//import org.swyp.linkit.domain.review.dto.response.ReviewDetailResponseDto;
//import org.swyp.linkit.domain.review.dto.response.ReviewDetailsResponseDto;
//import org.swyp.linkit.domain.review.dto.response.ReviewResponseDto;
//import org.swyp.linkit.domain.review.entity.Review;
//import org.swyp.linkit.domain.review.repository.ReviewRepository;
//import org.swyp.linkit.domain.review.repository.UserRatingStatRepository;
//import org.swyp.linkit.domain.review.repository.UserSkillRatingStatRepository;
//import org.swyp.linkit.domain.user.entity.*;
//import org.swyp.linkit.domain.user.repository.SkillCategoryRepository;
//import org.swyp.linkit.domain.user.repository.UserProfileRepository;
//import org.swyp.linkit.domain.user.repository.UserRepository;
//import org.swyp.linkit.domain.user.repository.UserSkillRepository;
//import org.swyp.linkit.global.error.exception.ReviewNotFoundException;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//@Transactional
//@Import(TestRedisConfig.class)
//@ActiveProfiles("test")
//@SpringBootTest(properties = "scheduler.enabled=false")
//@DisplayName("ReviewServiceImpl 통합 테스트")
//class ReviewServiceImplIntegrationTest {
//
//    @Autowired
//    private EntityManager em;
//    @Autowired
//    private ReviewService reviewService;
//    @Autowired
//    private ReviewRepository reviewRepository;
//    @Autowired
//    private UserRatingStatRepository userRatingStatRepository;
//    @Autowired
//    private UserSkillRatingStatRepository userSkillRatingStatRepository;
//    @Autowired
//    private SkillCategoryRepository skillCategoryRepository;
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private UserProfileRepository profileRepository;
//    @Autowired
//    private UserSkillRepository userSkillRepository;
//    @Autowired
//    private SkillExchangeRepository exchangeRepository;
//
//    private User reviewer;
//    private User reviewee;
//    private UserSkill revieweeSkill;
//    private SkillExchange skillExchange;
//    private SkillCategory skillCategory;
//    private UserProfile revieweeProfile;
//    private final String defaultContent = "content";
//    private final String defaultSkillName = "skillName";
//
//    @BeforeEach
//    void setup() {
//        // 스킬 카테고리 생성
//        skillCategory = createSavedSkillCategory();
//
//        // 리뷰어 생성
//        reviewer = createUser();
//        userRepository.save(reviewer);
//
//        // 리뷰 대상자 생성
//        reviewee = createUser();
//        userRepository.save(reviewee);
//
//        // 리뷰 대상자 프로필 생성
//        revieweeProfile = createProfile(reviewee);
//
//        // 리뷰 대상자 스킬 생성
//        revieweeSkill = createUserSkill(skillCategory, "Java");
//        revieweeProfile.addUserSkill(revieweeSkill);
//        profileRepository.save(revieweeProfile);
//        userSkillRepository.save(revieweeSkill);
//
//        // 스킬 거래 생성
//        skillExchange = createSkillExchange(reviewer, reviewee, revieweeSkill);
//        skillExchange.updateExchangeStatus(ExchangeStatus.COMPLETED);
//        exchangeRepository.save(skillExchange);
//
//        em.flush();
//        em.clear();
//    }
//
//    @Nested
//    @DisplayName("리뷰, 평점 작성")
//    class CreateReview {
//
//        @Nested
//        @DisplayName("성공 케이스")
//        class SuccessCases {
//            @Test
//            @DisplayName("사용자는 완료된(COMPLETED) 스킬 거래에 리뷰를 작성할 수 있다.")
//            public void success() {
//                // given
//                ReviewRequestDto reviewRequestDto = new ReviewRequestDto(
//                        skillExchange.getId(),
//                        revieweeSkill.getId(),
//                        reviewee.getId(),
//                        defaultContent,
//                        3);
//
//                ReviewDto reviewDto = ReviewDto.ofCreate(reviewer.getId(), reviewRequestDto);
//
//                // when
//                ReviewResponseDto responseDto = reviewService.createReview(reviewDto);
//
//                // then
//                // responseDto 검증
//                assertThat(responseDto.getReviewId()).isNotNull();
//                assertThat(responseDto.getContent()).isEqualTo(reviewDto.getContent());
//                assertThat(responseDto.getRating()).isEqualTo(reviewDto.getRating());
//
//                // 리뷰 DB 검증
//                Long reviewId = responseDto.getReviewId();
//                assertThat(reviewRepository.findById(reviewId))
//                        .isPresent()
//                        .get()
//                        .satisfies(r -> {
//                            assertThat(r.getSkillExchangeId()).isEqualTo(skillExchange.getId());
//                            assertThat(r.getReviewerId()).isEqualTo(reviewDto.getReviewerId());
//                            assertThat(r.getRevieweeSkillId()).isEqualTo(reviewDto.getRevieweeSkillId());
//                            assertThat(r.getRevieweeId()).isEqualTo(reviewDto.getRevieweeId());
//                            assertThat(r.getContent()).isEqualTo(reviewDto.getContent());
//                            assertThat(r.getRating()).isEqualTo(reviewDto.getRating());
//                            assertThat(r.getCreatedAt()).isNotNull();
//                        });
//
//                // 유저 평점 DB 검증
//                assertThat(userRatingStatRepository.findByUserId(reviewee.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(stat -> {
//                            assertThat(stat.getId()).isNotNull();
//                            assertThat(stat.getUserId()).isEqualTo(reviewee.getId());
//                            assertThat(stat.getRatingCount()).isEqualTo(1);
//                            assertThat(stat.getRatingSum()).isEqualTo(reviewDto.getRating());
//                            assertThat(stat.getCreatedAt()).isNotNull();
//                        });
//
//                // 유저 스킬 평점 DB 검증
//                assertThat(userSkillRatingStatRepository.findByUserSkillId(revieweeSkill.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(stat -> {
//                            assertThat(stat.getId()).isNotNull();
//                            assertThat(stat.getUserSkillId()).isEqualTo(revieweeSkill.getId());
//                            assertThat(stat.getRatingCount()).isEqualTo(1);
//                            assertThat(stat.getRatingSum()).isEqualTo(reviewDto.getRating());
//                            assertThat(stat.getStar1Count()).isEqualTo(0);
//                            assertThat(stat.getStar2Count()).isEqualTo(0);
//                            assertThat(stat.getStar3Count()).isEqualTo(1);
//                            assertThat(stat.getStar4Count()).isEqualTo(0);
//                            assertThat(stat.getStar5Count()).isEqualTo(0);
//                            assertThat(stat.getCreatedAt()).isNotNull();
//                        });
//            }
//        }
//
//        @Nested
//        @DisplayName("실패 케이스")
//        class FailCases {
//        }
//    }
//
//    @Nested
//    @DisplayName("리뷰, 평점 수정")
//    class UpdateReview {
//
//        @Nested
//        @DisplayName("성공 케이스")
//        class SuccessCases {
//            @Test
//            @DisplayName("사용자는 작성한 리뷰, 평점을 수정할 수 있다.")
//            public void success() {
//                // given
//                // 리뷰 생성
//                int oldRating = 3;
//                ReviewRequestDto reviewRequestDto = new ReviewRequestDto(
//                        skillExchange.getId(),
//                        revieweeSkill.getId(),
//                        reviewee.getId(),
//                        defaultContent,
//                        oldRating);
//
//                ReviewResponseDto createResponseDto = reviewService.createReview(ReviewDto.ofCreate(reviewer.getId(), reviewRequestDto));
//                em.flush();
//                em.clear();
//
//                // 리뷰 수정 requestDto
//                Long reviewId = createResponseDto.getReviewId();
//                String newContent = "newContent";
//                int newRating = 5;
//
//                ReviewUpdateRequestDto reviewUpdateRequestDto = new ReviewUpdateRequestDto(newContent, newRating);
//                ReviewDto reviewDto = ReviewDto.ofUpdate(reviewer.getId(), reviewId, reviewUpdateRequestDto);
//
//                // when
//                ReviewResponseDto responseDto = reviewService.updateReview(reviewDto);
//
//                // then
//                // responseDto 검증
//                assertThat(responseDto.getReviewId()).isEqualTo(reviewId);
//                assertThat(responseDto.getContent()).isEqualTo(reviewDto.getContent());
//                assertThat(responseDto.getRating()).isEqualTo(reviewDto.getRating());
//
//                // 리뷰 DB 검증
//                assertThat(reviewRepository.findById(reviewId))
//                        .isPresent()
//                        .get()
//                        .satisfies(r -> {
//                            assertThat(r.getId()).isEqualTo(reviewId);
//                            assertThat(r.getSkillExchangeId()).isEqualTo(skillExchange.getId());
//                            assertThat(r.getReviewerId()).isEqualTo(reviewDto.getReviewerId());
//                            assertThat(r.getRevieweeSkillId()).isEqualTo(revieweeSkill.getId());
//                            assertThat(r.getRevieweeId()).isEqualTo(reviewee.getId());
//                            assertThat(r.getContent()).isEqualTo(reviewDto.getContent());
//                            assertThat(r.getRating()).isEqualTo(reviewDto.getRating());
//                            assertThat(r.getCreatedAt()).isNotNull();
//                        });
//
//                // 유저 평점 DB 검증
//                assertThat(userRatingStatRepository.findByUserId(reviewee.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(stat -> {
//                            assertThat(stat.getId()).isNotNull();
//                            assertThat(stat.getUserId()).isEqualTo(reviewee.getId());
//                            assertThat(stat.getRatingCount()).isEqualTo(1);
//                            assertThat(stat.getRatingSum()).isEqualTo(reviewDto.getRating());
//                            assertThat(stat.getCreatedAt()).isNotNull();
//                        });
//
//                // 유저 스킬 평점 DB 검증
//                assertThat(userSkillRatingStatRepository.findByUserSkillId(revieweeSkill.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(stat -> {
//                            assertThat(stat.getId()).isNotNull();
//                            assertThat(stat.getUserSkillId()).isEqualTo(revieweeSkill.getId());
//                            assertThat(stat.getRatingCount()).isEqualTo(1);
//                            assertThat(stat.getRatingSum()).isEqualTo(reviewDto.getRating());
//                            assertThat(stat.getStar1Count()).isEqualTo(0);
//                            assertThat(stat.getStar2Count()).isEqualTo(0);
//                            assertThat(stat.getStar3Count()).isEqualTo(0);
//                            assertThat(stat.getStar4Count()).isEqualTo(0);
//                            assertThat(stat.getStar5Count()).isEqualTo(1);
//                            assertThat(stat.getCreatedAt()).isNotNull();
//                        });
//            }
//        }
//
//        @Nested
//        @DisplayName("실패 케이스")
//        class FailCases {
//        }
//    }
//
//    @Nested
//    @DisplayName("리뷰 삭제")
//    class DeleteReview {
//
//        @Nested
//        @DisplayName("성공 케이스")
//        class SuccessCases {
//            @Test
//            @DisplayName("유저는 자신이 작성한 리뷰를 삭제할 수 있습니다.")
//            public void success() {
//                // given
//                // 리뷰 생성
//                ReviewRequestDto reviewRequestDto = new ReviewRequestDto(
//                        skillExchange.getId(),
//                        revieweeSkill.getId(),
//                        reviewee.getId(),
//                        defaultContent,
//                        3);
//
//                ReviewDto reviewDto = ReviewDto.ofCreate(reviewer.getId(), reviewRequestDto);
//                ReviewResponseDto responseDto = reviewService.createReview(reviewDto);
//
//                // 삭제할 리뷰 정보
//                Long reviewId = responseDto.getReviewId();
//                Long reviewerId = reviewer.getId();
//
//                // when
//                reviewService.deleteReview(reviewId, reviewerId);
//
//                // then
//                // DB 검증
//                // 리뷰 DB 검증
//                assertThat(reviewRepository.findById(reviewId)).isNotPresent();
//
//                // 유저 평점 DB 검증
//                assertThat(userRatingStatRepository.findByUserId(reviewee.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(stat -> {
//                            assertThat(stat.getId()).isNotNull();
//                            assertThat(stat.getUserId()).isEqualTo(reviewee.getId());
//                            assertThat(stat.getRatingCount()).isEqualTo(0);
//                            assertThat(stat.getRatingSum()).isEqualTo(0);
//                            assertThat(stat.getCreatedAt()).isNotNull();
//                        });
//
//                // 유저 스킬 평점 DB 검증
//                assertThat(userSkillRatingStatRepository.findByUserSkillId(revieweeSkill.getId()))
//                        .isPresent()
//                        .get()
//                        .satisfies(stat -> {
//                            assertThat(stat.getId()).isNotNull();
//                            assertThat(stat.getUserSkillId()).isEqualTo(revieweeSkill.getId());
//                            assertThat(stat.getRatingCount()).isEqualTo(0);
//                            assertThat(stat.getRatingSum()).isEqualTo(0);
//                            assertThat(stat.getStar1Count()).isEqualTo(0);
//                            assertThat(stat.getStar2Count()).isEqualTo(0);
//                            assertThat(stat.getStar3Count()).isEqualTo(0);
//                            assertThat(stat.getStar4Count()).isEqualTo(0);
//                            assertThat(stat.getStar5Count()).isEqualTo(0);
//                            assertThat(stat.getCreatedAt()).isNotNull();
//                        });
//            }
//        }
//
//        @Nested
//        @DisplayName("실패 케이스")
//        class FailCases {
//        }
//    }
//
//    @Nested
//    @DisplayName("리뷰 단건 조회")
//    class GetReview {
//
//        @Nested
//        @DisplayName("성공 케이스")
//        class SuccessCases {
//            @Test
//            @DisplayName("리뷰 단건 조회 성공")
//            public void success() {
//                // given
//                // 리뷰 생성
//                ReviewRequestDto reviewRequestDto = new ReviewRequestDto(
//                        skillExchange.getId(),
//                        revieweeSkill.getId(),
//                        reviewee.getId(),
//                        defaultContent,
//                        3);
//
//                ReviewDto reviewDto = ReviewDto.ofCreate(reviewer.getId(), reviewRequestDto);
//                ReviewResponseDto responseDto = reviewService.createReview(reviewDto);
//
//                Long reviewId = responseDto.getReviewId();
//
//                // when
//                ReviewResponseDto result = reviewService.getReview(reviewer.getId(), reviewId);
//
//                // then
//                assertThat(result.getReviewId()).isEqualTo(reviewId);
//                assertThat(result.getRating()).isEqualTo(reviewRequestDto.getRating());
//                assertThat(result.getContent()).isEqualTo(reviewRequestDto.getContent());
//            }
//        }
//
//        @Nested
//        @DisplayName("실패 케이스")
//        class FailCases {
//            @Test
//            @DisplayName("리뷰가 존재하지 않아 ReviewNotFoundException 발생.")
//            public void fail_ReviewNotFoundException() {
//                // given
//                Long notExistsReviewId = Long.MAX_VALUE;
//
//                // when && then
//                assertThatThrownBy(() -> reviewService.getReview(reviewer.getId(), notExistsReviewId)).isInstanceOf(ReviewNotFoundException.class);
//            }
//        }
//    }
//
//
//    // todo 간단한 테스트 추후 고도화
//    @Nested
//    @DisplayName("리뷰 페이징 조회 테스트")
//    class ReviewPagingQueryTests {
//
//        private List<Review> reviews = new ArrayList<>();
//
//        @BeforeEach
//        void setupMultipleReviews() {
//            // 리뷰 5개 생성
//            for (int i = 1; i <= 5; i++) {
//                Review review = Review.create(
//                        skillExchange.getId(),
//                        revieweeSkill.getId(),
//                        reviewer.getId(),
//                        reviewee.getId(),
//                        defaultContent + i,
//                        (i % 5) + 1 // 평점 1~5
//                );
//                reviews.add(reviewRepository.save(review));
//            }
//            em.flush();
//            em.clear();
//        }
//
//        @Test
//        @DisplayName("getReceivedReviews: 유저가 받은 모든 리뷰를 최신순으로 조회한다.")
//        void getReceivedReviews_All() {
//            // given
//            Long userId = reviewee.getId();
//            Long skillId = revieweeSkill.getId();
//            Long cursorId = null;
//            int size = 3;
//
//            // when
//            ReviewDetailsResponseDto response = reviewService.getReceivedReviews(userId, skillId, cursorId, size);
//
//            // then
//            assertThat(response.getContents()).hasSize(3);
//            assertThat(response.isHasNext()).isTrue();
//            assertThat(response.getNextCursor()).isEqualTo(reviews.get(5 - size).getId());
//
//            for (int i = 0; i < size; i++) {
//                int reviewNumber = 5 - i;
//                ReviewDetailResponseDto detail = response.getContents().get(i);
//
//                assertThat(detail.getReviewId()).isNotNull();
//                assertThat(detail.getContent()).isEqualTo(defaultContent + reviewNumber);
//                assertThat(detail.getRating()).isEqualTo((reviewNumber % 5) + 1);
//                assertThat(detail.getReviewerNickname()).isEqualTo(reviewer.getNickname());
//                assertThat(detail.getSkillName()).isEqualTo(revieweeSkill.getSkillName());
//                assertThat(detail.getCreatedDateTime()).isNotNull();
//            }
//        }
//
//        @Test
//        @DisplayName("getReceivedReviews: 특정 스킬에 대해서만 받은 리뷰를 조회한다.")
//        void getReceivedReviews_WithSkill() {
//            // when
//            ReviewDetailsResponseDto response = reviewService.getReceivedReviews(reviewee.getId(), revieweeSkill.getId(), null, 10);
//
//            // then
//            assertThat(response.getContents()).hasSize(5);
//            assertThat(response.getContents().get(0).getSkillName()).isEqualTo(revieweeSkill.getSkillName());
//        }
//
//        @Test
//        @DisplayName("getWrittenReviews: 유저가 작성한 리뷰를 커서 기반으로 페이징 조회한다.")
//        void getWrittenReviews_Paging() {
//            // given
//            ReviewDetailsResponseDto firstPage = reviewService.getWrittenReviews(reviewer.getId(), null, 2);
//            Long cursorId = firstPage.getContents().get(1).getReviewId();
//
//            // when
//            ReviewDetailsResponseDto secondPage = reviewService.getWrittenReviews(reviewer.getId(), cursorId, 2);
//
//            // then
//            assertThat(secondPage.getContents()).hasSize(2);
//            assertThat(secondPage.getContents().get(0).getReviewId()).isLessThan(cursorId);
//        }
//
//        @Test
//        @DisplayName("getSkillReviews: 특정 스킬의 전체 리뷰를 조회한다.")
//        void getSkillReviews_Success() {
//            // when
//            ReviewDetailsResponseDto response = reviewService.getSkillReviews(revieweeSkill.getId(), null, 5);
//
//            // then
//            assertThat(response.getContents()).isNotEmpty();
//            assertThat(response.getContents().get(0).getSkillName()).isEqualTo(revieweeSkill.getSkillName());
//        }
//    }
//
//    private SkillExchange createSkillExchange(User requester, User receiver, UserSkill receiverSkill) {
//        return SkillExchange.create(
//                requester,
//                receiver,
//                receiverSkill,
//                LocalDate.now().plusDays(2),
//                LocalTime.now(),
//                LocalTime.now().plusMinutes(30),
//                "message"
//        );
//    }
//
//    private Review createReview(Long skillExchangeId, Long revieweeSkillId, Long reviewerId, Long revieweeId, int rating) {
//        return Review.create(skillExchangeId, revieweeSkillId, reviewerId,
//                revieweeId, "defaultContent", rating);
//    }
//
//    private UserProfile createProfile(User user) {
//        return UserProfile.create(user, "description",
//                ExchangeType.OFFLINE, null, null);
//    }
//
//    private SkillCategory createSavedSkillCategory() {
//        return skillCategoryRepository.findByCategoryType(SkillCategoryType.DEVELOPMENT)
//                .orElseGet(() -> skillCategoryRepository.save(SkillCategory.create(SkillCategoryType.DEVELOPMENT)));
//    }
//
//    private UserSkill createUserSkill(SkillCategory skillCategory, String skillName) {
//        return UserSkill.create(skillCategory, skillName, "title", SkillProficiency.MEDIUM,
//                "description", 30);
//    }
//
//    private User createUser() {
//        String uuid = UUID.randomUUID().toString();
//        return User.create(
//                OAuthProvider.KAKAO,
//                "kakao" + uuid,
//                uuid + "email@example.com",
//                "name" + uuid,
//                "https://image",
//                "nickname" + uuid);
//    }
//}