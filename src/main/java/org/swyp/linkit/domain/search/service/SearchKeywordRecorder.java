package org.swyp.linkit.domain.search.service;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.search.repository.SearchKeywordStatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchKeywordRecorder {

    private final SearchKeywordStatRepository searchKeywordStatRepository;

    // 검색어 집계 (일별 카운트 증가)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String keyword) {
        searchKeywordStatRepository.upsertIncrement(LocalDate.now(), keyword);
        log.debug("검색어 집계: keyword='{}', date={}", keyword, LocalDate.now());
    }
}