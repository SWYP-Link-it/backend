package org.swyp.linkit.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.user.entity.SkillCategory;
import org.swyp.linkit.domain.user.entity.SkillCategoryType;
import org.swyp.linkit.domain.user.repository.SkillCategoryRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 애플리케이션 시작 시 SkillCategoryType enum 과 skill_category 테이블 동기화
 * - enum 에는 있지만 DB 에 없는 카테고리만 추가 (기존 데이터는 변경하지 않음)
 * - 카테고리 row 누락으로 스킬 등록/수정 시 SKILL_CATEGORY_NOT_FOUND 가 발생하는 것을 방지
 * - 다른 Runner(부하 테스트 시더 등)보다 먼저 실행
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SkillCategoryInitializer implements ApplicationRunner {

    private final SkillCategoryRepository skillCategoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Set<SkillCategoryType> existingTypes = skillCategoryRepository.findAll().stream()
                .map(SkillCategory::getCategoryType)
                .collect(Collectors.toSet());

        List<SkillCategory> missingCategories = Arrays.stream(SkillCategoryType.values())
                .filter(type -> !existingTypes.contains(type))
                .map(SkillCategory::create)
                .toList();

        if (missingCategories.isEmpty()) {
            return;
        }

        skillCategoryRepository.saveAll(missingCategories);

        log.info("스킬 카테고리 초기화: 추가된 카테고리={}",
                missingCategories.stream().map(SkillCategory::getCategoryType).toList());
    }
}
