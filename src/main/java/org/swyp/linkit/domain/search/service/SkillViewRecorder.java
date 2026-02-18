package org.swyp.linkit.domain.search.service;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.search.repository.SkillViewStatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkillViewRecorder {

    private final SkillViewStatRepository skillViewStatRepository;

    // 스킬 조회수 집계 (일별 카운트 증가)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long skillId) {
        skillViewStatRepository.upsertIncrement(LocalDate.now(), skillId);
        log.debug("스킬 조회수 집계: skillId={}, date={}", skillId, LocalDate.now());
    }
}