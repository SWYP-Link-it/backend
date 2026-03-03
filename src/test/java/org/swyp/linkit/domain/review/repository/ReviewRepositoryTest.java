//package org.swyp.linkit.domain.review.repository;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.context.annotation.Import;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Slice;
//import org.swyp.linkit.domain.exchange.entity.SkillExchange;
//import org.swyp.linkit.domain.review.entity.Review;
//import org.swyp.linkit.domain.review.repository.projection.ReviewDetailQuery;
//import org.swyp.linkit.domain.user.entity.*;
//import org.swyp.linkit.global.config.JpaAuditingConfig;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@Import(JpaAuditingConfig.class)
//@DisplayName("ReviewRepositoryTest 단위 테스트")
//class ReviewRepositoryTest {
//
//    @Autowired
//    TestEntityManager em;
//
//    @Autowired
//    ReviewRepository reviewRepository;
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
//        skillCategory = createSkillCategory();
//        em.persist(skillCategory);
//
//        // 리뷰어 생성
//        reviewer = createUser();
//        em.persist(reviewer);
//
//        // 리뷰 대상자 생성
//        reviewee = createUser();
//        em.persist(reviewee);
//
//        // 리뷰 대상자 프로필 생성
//        revieweeProfile = createProfile(reviewee);
//        em.persist(revieweeProfile);
//
//        // 리뷰 대상자 스킬 생성
//        revieweeSkill = createUserSkill(skillCategory, "Java");
//        revieweeProfile.addUserSkill(revieweeSkill);
//        em.persist(revieweeSkill);
//
//        // 스킬 거래 생성
//        skillExchange = createSkillExchange(reviewer, reviewee, revieweeSkill);
//        em.persist(skillExchange);
//
//        em.flush();
//        em.clear();
//    }
//
//    @Nested
//    @DisplayName("스킬 거래 ID, 리뷰 작성자 ID로 리뷰 존재 확인")
//    class ExistsBySkillExchangeIdAndReviewerId {
//        @Test
//        @DisplayName("존재함")
//        public void exists() {
//            // given
//            // 리뷰 생성
//            int rating = 3;
//            Review review = createReview(
//                    skillExchange.getId(),
//                    revieweeSkill.getId(),
//                    reviewer.getId(),
//                    reviewee.getId(),
//                    rating);
//            em.persist(review);
//            em.flush();
//            em.clear();
//
//            // when
//            boolean exists = reviewRepository.existsBySkillExchangeIdAndReviewerId(skillExchange.getId(), reviewer.getId());
//
//            // then
//            assertThat(exists).isTrue();
//        }
//
//        @Test
//        @DisplayName("존재하지 않음")
//        public void none_exists() {
//            // when
//            boolean exists = reviewRepository.existsBySkillExchangeIdAndReviewerId(skillExchange.getId(), reviewer.getId());
//
//            // then
//            assertThat(exists).isFalse();
//        }
//    }
//
//    @Nested
//    @DisplayName("작성한 리뷰 페이징 조회")
//    class findAllByReviewerId {
//
//        @Test
//        @DisplayName("첫 페이지")
//        public void without_cursor_first_page() {
//            // given
//            // 5개의 작성한 리뷰 생성
//            create5Reviews();
//
//            Long cursorId = null;
//            int size = 3;
//            Pageable pageable = PageRequest.of(0, size);
//
//            em.flush();
//            em.clear();
//
//            // when
//            Slice<ReviewDetailQuery> slice = reviewRepository.findAllByReviewerId(reviewer.getId(), cursorId, pageable);
//
//            // then
//            assertThat(slice.getContent().size()).isEqualTo(size);
//            List<ReviewDetailQuery> contents = slice.getContent();
//            for(int i = 0; i < size; i++){
//                ReviewDetailQuery content = contents.get(i);
//                int expectedOrder = 5 - i;
//
//                // 조회 내역 검증
//                assertThat(content.reviewId()).isNotNull();
//                assertThat(content.reviewerNickname()).isEqualTo(reviewee.getNickname());
//                assertThat(content.skillName()).isEqualTo(revieweeSkill.getSkillName());
//                assertThat(content.content()).isEqualTo(expectedOrder + defaultContent);
//                assertThat(content.createdAt()).isNotNull();
//
//                // 내림차순 검증
//                if (i > 0) {
//                    assertThat(contents.get(i).reviewId()).isLessThan(contents.get(i - 1).reviewId());
//                }
//            }
//        }
//
//        @Test
//        @DisplayName("커서 기반")
//        public void with_cursor() {
//            // given
//            // 5개의 작성한 리뷰 생성
//            List<Long> reviewIds = create5Reviews();
//
//            Long cursorId = reviewIds.get(reviewIds.size() - 3);
//            int size = 3;
//            Pageable pageable = PageRequest.of(0, size);
//
//            em.flush();
//            em.clear();
//
//            // when
//            Slice<ReviewDetailQuery> slice = reviewRepository.findAllByReviewerId(reviewer.getId(), cursorId, pageable);
//
//            // then
//            int expectedSize = reviewIds.size() - 3;
//            assertThat(slice.getContent().size()).isEqualTo(expectedSize);
//        }
//    }
//
//    @Nested
//    @DisplayName("스킬 별 받은 리뷰 페이징 조회")
//    class FindAllByRevieweeId {
//
//        @Test
//        @DisplayName("첫 페이지_모든 리뷰")
//        public void without_cursor_first_page() {
//            // given
//            // 5개의 스킬 생성
//            List<Long> skillIds = create5Skills();
//
//            // 각각 스킬에 대한 리뷰 작성 (총 5개의 리뷰)
//            create5ReviewsFrom5Skills(skillIds);
//
//            Long cursorId = null;
//            int size = 3;
//            Pageable pageable = PageRequest.of(0, size);
//
//            em.flush();
//            em.clear();
//
//            // when
//            Slice<ReviewDetailQuery> slice = reviewRepository.findAllByRevieweeId(reviewee.getId(), null, cursorId, pageable);
//
//            // then
//            assertThat(slice.getContent().size()).isEqualTo(size);
//            List<ReviewDetailQuery> contents = slice.getContent();
//            for(int i = 0; i < size; i++){
//                ReviewDetailQuery content = contents.get(i);
//                int expectedOrder = 5 - i;
//
//                // 조회 내역 검증
//                assertThat(content.reviewId()).isNotNull();
//                assertThat(content.reviewerNickname()).isEqualTo(reviewer.getNickname());
//                assertThat(content.skillName()).isEqualTo(expectedOrder + defaultSkillName);
//                assertThat(content.content()).isEqualTo(expectedOrder + defaultContent);
//                assertThat(content.createdAt()).isNotNull();
//
//                // 내림차순 검증
//                if (i > 0) {
//                    assertThat(contents.get(i).reviewId()).isLessThan(contents.get(i - 1).reviewId());
//                }
//            }
//        }
//
//        @Test
//        @DisplayName("커서 기반_모든 리뷰")
//        public void with_cursor() {
//            // given
//            // 5개의 스킬 생성
//            List<Long> skillIds = create5Skills();
//
//            // 각각 스킬에 대한 리뷰 작성 (총 5개의 리뷰)
//            List<Long> reviewIds = create5ReviewsFrom5Skills(skillIds);
//
//            Long cursorId = reviewIds.get(reviewIds.size() - 3);
//            int size = 3;
//            Pageable pageable = PageRequest.of(0, size);
//
//            em.flush();
//            em.clear();
//
//            // when
//            Slice<ReviewDetailQuery> slice = reviewRepository.findAllByRevieweeId(reviewee.getId(), null, cursorId, pageable);
//
//            // then
//            int expectedSize = reviewIds.size() - 3;
//            assertThat(slice.getContent().size()).isEqualTo(expectedSize);
//        }
//
//        @Test
//        @DisplayName("첫 페이지_특정 스킬 리뷰")
//        public void without_cursor_first_page_selectedReview() {
//            // given
//            // 5개의 스킬 생성
//            List<Long> skillIds = create5Skills();
//
//            // 각각 스킬에 대한 리뷰 작성 (총 5개의 리뷰)
//            create5ReviewsFrom5Skills(skillIds);
//
//            // 3번째 스킬로 조회
//            Long skillId = skillIds.get(2);
//            Long cursorId = null;
//            int size = 3;
//            Pageable pageable = PageRequest.of(0, size);
//
//            em.flush();
//            em.clear();
//
//            // when
//            Slice<ReviewDetailQuery> slice = reviewRepository.findAllByRevieweeId(reviewee.getId(), skillId, cursorId, pageable);
//
//            // then
//            assertThat(slice.getContent().size()).isEqualTo(1);
//        }
//    }
//
//    @Nested
//    @DisplayName("스킬 리뷰 페이징 조회")
//    class FindAllBySkillId {
//
//        @Test
//        @DisplayName("첫 페이지")
//        public void without_cursor_first_page() {
//            // given
//            // 5개의 스킬 생성
//            List<Long> skillIds = create5Skills();
//
//            // 각각 스킬에 대한 리뷰 작성 (총 5개의 리뷰)
//            create5ReviewsFrom5Skills(skillIds);
//
//            Long skillId = skillIds.get(0);
//            Long cursorId = null;
//            int size = 3;
//            Pageable pageable = PageRequest.of(0, size);
//
//            em.flush();
//            em.clear();
//
//            // when
//            Slice<ReviewDetailQuery> slice = reviewRepository.findAllBySkillId(skillId, cursorId, pageable);
//
//            // then
//            assertThat(slice.getContent().size()).isEqualTo(1);
//            List<ReviewDetailQuery> contents = slice.getContent();
//            ReviewDetailQuery content = contents.get(0);
//
//            assertThat(content.reviewId()).isNotNull();
//            assertThat(content.reviewerNickname()).isEqualTo(reviewer.getNickname());
//            assertThat(content.skillName()).isEqualTo(1 + defaultSkillName);
//            assertThat(content.content()).isEqualTo(1 + defaultContent);
//            assertThat(content.createdAt()).isNotNull();
//
//        }
//
//        @Test
//        @DisplayName("첫 페이지_특정 스킬 리뷰")
//        public void without_cursor_first_page_selectedReview() {
//            // given
//            // 5개의 스킬 생성
//            List<Long> skillIds = create5Skills();
//
//            // 각각 스킬에 대한 리뷰 작성 (총 5개의 리뷰)
//            create5ReviewsFrom5Skills(skillIds);
//
//            // 3번째 스킬로 조회
//            Long skillId = skillIds.get(2);
//            Long cursorId = null;
//            int size = 3;
//            Pageable pageable = PageRequest.of(0, size);
//
//            em.flush();
//            em.clear();
//
//            // when
//            Slice<ReviewDetailQuery> slice = reviewRepository.findAllBySkillId(skillId, cursorId, pageable);
//
//            // then
//            assertThat(slice.getContent().size()).isEqualTo(1);
//        }
//    }
//
//    private List<Long> create5ReviewsFrom5Skills(List<Long> skillIds) {
//        List<Long> reviewIds = new ArrayList<>();
//        for(int i = 1; i <= 5; i++){
//
//            Review review = em.persist(Review.create(
//                    skillExchange.getId(),
//                    skillIds.get(i - 1),
//                    reviewer.getId(),
//                    reviewee.getId(),
//                    i + defaultContent,
//                    3)
//            );
//            reviewIds.add(review.getId());
//        }
//        return reviewIds;
//    }
//
//    private List<Long> create5Skills() {
//        List<Long> skillIds = new ArrayList<>();
//        for(int i = 1; i <= 5; i++){
//
//            UserSkill userSkill = createUserSkill(
//                    skillCategory,
//                    i + defaultSkillName);
//
//            revieweeProfile.addUserSkill(userSkill);
//            em.persist(userSkill);
//            skillIds.add(userSkill.getId());
//        }
//        return skillIds;
//    }
//
//    private List<Long> create5Reviews() {
//        List<Long> ids = new ArrayList<>();
//
//        for(int i = 1; i <= 5; i++){
//            Review review = em.persist(Review.create(
//                    skillExchange.getId(),
//                    revieweeSkill.getId(),
//                    reviewer.getId(),
//                    reviewee.getId(),
//                    i + defaultContent,
//                    3)
//            );
//            ids.add(review.getId());
//        }
//        return ids;
//    }
//
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
//    private Review createReview(Long skillExchangeId, Long revieweeSkillId, Long reviewerId, Long revieweeId, int rating){
//        return Review.create(skillExchangeId, revieweeSkillId, reviewerId,
//                revieweeId, "defaultContent", rating);
//    }
//
//    private UserProfile createProfile(User user) {
//        return UserProfile.create(user, "description",
//                ExchangeType.OFFLINE, null, null);
//    }
//
//    private SkillCategory createSkillCategory() {
//        return SkillCategory.create(SkillCategoryType.DEVELOPMENT);
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