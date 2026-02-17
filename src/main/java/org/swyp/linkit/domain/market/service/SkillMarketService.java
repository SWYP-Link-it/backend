package org.swyp.linkit.domain.market.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.market.dto.response.SkillCardPageResponseDto;
import org.swyp.linkit.domain.market.dto.response.SkillDetailDto;
import org.swyp.linkit.domain.search.service.SearchKeywordRecorder;
import org.swyp.linkit.domain.search.service.SearchService;
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
		private final SearchService searchService;
    private final SearchKeywordRecorder searchKeywordRecorder;

		/**
		 * 스킬 장터 목록 커서 기반 페이징 조회
		 * - category, keyword 는 선택적 필터
		 * - cursorId : null 이면 첫 페이지, 값이 있으면 해당 id 미만 조회
		 * - 첫 페이지 size = 11 (배너 영역 고려), 이후 size = 12
		 * - 검색어 집계는 SearchService 에 위임 (별도 트랜잭션)
		 */
		@Transactional(readOnly = true)
		public SkillCardPageResponseDto getVisibleSkills(SkillCategoryType category, String searchKeyword,
																											Long cursorId, int size) {
				// 1. keyword trim 처리
				String trimmedKeyword = (searchKeyword != null && !searchKeyword.isBlank())
								? searchKeyword.trim()
								: null;

				// 2. 검색어 집계 (keyword 가 있을 때만, 별도 트랜잭션)
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

    // 스킬 ID로 상세 정보 조회 (스킬 + 프로필 전체)
    @Transactional(readOnly = true)
    public SkillDetailDto getSkillDetail(Long skillId) {
        // 1. 메인 스킬 조회 (이미지 포함)
        UserSkill mainSkill = userSkillRepository.findVisibleSkillDetailById(skillId)
                .orElseThrow(() -> new UserSkillNotFoundException("해당 스킬을 찾을 수 없거나 현재 노출 중이 아닙니다."));

        // 2. 조회수 집계
        searchService.recordSkillView(skillId);

        // 3. 해당 사용자의 모든 스킬 조회 (이미지 포함)
        Long userId = mainSkill.getOwnerId();
        List<UserSkill> allUserSkills = userSkillRepository.findVisibleSkillsWithImagesByUserId(userId);

        log.info("스킬 상세 정보 조회: skillId={}, userId={}, totalSkillsCount={}",
                skillId, userId, allUserSkills.size());

        // 4. DTO 변환
        return SkillDetailDto.from(mainSkill, allUserSkills);
    }
}