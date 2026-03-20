package org.swyp.linkit.domain.market.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.market.dto.response.SkillCardPageResponseDto;
import org.swyp.linkit.domain.market.dto.response.SkillDetailDto;
import org.swyp.linkit.domain.market.dto.response.SkillSitemapResponseDto;
import org.swyp.linkit.domain.market.dto.response.SkillRatingResponseDto;
import org.swyp.linkit.domain.review.repository.ReviewRepository;
import org.swyp.linkit.domain.search.service.SearchKeywordRecorder;
import org.swyp.linkit.domain.search.service.SkillViewRecorder;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.repository.UserSkillRepository;
import org.swyp.linkit.global.error.exception.UserSkillNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillMarketService {

    private final UserSkillRepository userSkillRepository;
    private final ReviewRepository reviewRepository;
    private final SearchKeywordRecorder searchKeywordRecorder;
    private final SkillViewRecorder skillViewRecorder;

    /**
	 * 스킬 장터 목록 커서 기반 페이징 조회
	 * - category, keyword 는 선택적 필터
	 * - cursorId : null 이면 첫 페이지, 값이 있으면 해당 id 미만 조회
	 * - 첫 페이지 size = 11 (배너 영역 고려), 이후 size = 12
	 * - 검색어 집계는 SearchKeywordRecorder 에 위임 (Redis INCR)
	 */
    @Transactional(readOnly = true)
    public SkillCardPageResponseDto getVisibleSkills(SkillCategoryType category, String searchKeyword,
            Long cursorId, int size) {
        // 1. keyword trim 처리
        String trimmedKeyword = (searchKeyword != null && !searchKeyword.isBlank())
                ? searchKeyword.trim()
                : null;

        // 2. 검색어 집계 (keyword 가 있을 때만, Redis INCR)
        if (trimmedKeyword != null) {
            searchKeywordRecorder.record(trimmedKeyword);
        }

        // 3. 커서 기반 페이징 조회
        Pageable pageable = PageRequest.of(0, size);
        Slice<UserSkill> slice = userSkillRepository
                .findVisibleSkillsWithCursor(category, trimmedKeyword, cursorId, pageable);

        log.info("스킬 장터 목록 조회: category={}, keyword={}, cursorId={}, size={}, resultCount={}, hasNext={}",
                category, trimmedKeyword, cursorId, size,
                slice.getNumberOfElements(), slice.hasNext());

        // 4. 응답 DTO 변환
        return SkillCardPageResponseDto.of(slice);
    }

    // 사이트맵용 스킬 목록 조회 (skillId + modifiedAt)
    @Transactional(readOnly = true)
    public List<SkillSitemapResponseDto> getSkillsForSitemap() {
        return userSkillRepository.findVisibleSkillsForSitemap().stream()
                .map(SkillSitemapResponseDto::from)
                .toList();
    }

    // 스킬 ID로 상세 정보 조회 (스킬 + 프로필 전체 + 평점)
    @Transactional(readOnly = true)
    public SkillDetailDto getSkillDetail(Long skillId) {
        // 1. 메인 스킬 조회 (이미지 포함)
        UserSkill mainSkill = userSkillRepository.findVisibleSkillDetailById(skillId)
                .orElseThrow(() -> new UserSkillNotFoundException("해당 스킬을 찾을 수 없거나 현재 노출 중이 아닙니다."));

        // 2. 조회수 집계 (Redis INCR)
        skillViewRecorder.record(skillId);

        // 3. 해당 사용자의 모든 스킬 조회 (이미지 포함)
        Long userId = mainSkill.getOwnerId();
        List<UserSkill> allUserSkills = userSkillRepository.findVisibleSkillsWithImagesByUserId(userId);

        // 4. 유저 평점 조회 (review 테이블 직접 집계)
        double userAvgRating = Math.round(reviewRepository.findAvgRatingByRevieweeId(userId) * 10) / 10.0;

        // 5. 스킬 평균 평점 + 별점 분포 조회
        double skillAvgRating = Math.round(reviewRepository.findAvgRatingBySkillId(skillId) * 10) / 10.0;
        List<Object[]> dist = reviewRepository.findStarDistributionBySkillId(skillId);
        SkillRatingResponseDto skillRating = buildSkillRating(skillAvgRating, dist);

        log.info("스킬 상세 정보 조회: skillId={}, userId={}, totalSkillsCount={}",
                skillId, userId, allUserSkills.size());

        // 6. DTO 변환
        return SkillDetailDto.from(mainSkill, allUserSkills, userAvgRating, skillRating);
    }

    private SkillRatingResponseDto buildSkillRating(double skillAvgRating, List<Object[]> dist) {
        long[] starCounts = new long[6]; // 인덱스 1~5 사용
        long total = 0;
        for (Object[] row : dist) {
            int rating = (Integer) row[0];
            long count = (Long) row[1];
            if (rating >= 1 && rating <= 5) {
                starCounts[rating] = count;
                total += count;
            }
        }
        if (total == 0) {
            return SkillRatingResponseDto.of(skillAvgRating, 0, 0, 0, 0, 0);
        }
        return SkillRatingResponseDto.of(
                skillAvgRating,
                (int) Math.round(starCounts[1] * 100.0 / total),
                (int) Math.round(starCounts[2] * 100.0 / total),
                (int) Math.round(starCounts[3] * 100.0 / total),
                (int) Math.round(starCounts[4] * 100.0 / total),
                (int) Math.round(starCounts[5] * 100.0 / total)
        );
    }
}
