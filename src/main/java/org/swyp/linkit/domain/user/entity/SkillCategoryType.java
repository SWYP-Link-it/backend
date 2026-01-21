package org.swyp.linkit.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SkillCategoryType {

    IT_DEV("IT · 개발"),
    DESIGN_CREATIVE("디자인 · 크리에이티브"),
    VIDEO_PHOTO("영상 · 사진 · 편집"),
    MARKETING_CONTENT("마케팅 · 콘텐츠"),
    CAREER_JOB("커리어 · 취업"),
    LANGUAGE("외국어"),
    UNIVERSITY_MAJOR("대학 · 전공"),
    FINANCE_ECONOMY("재테크 · 경제"),
    SPORTS("운동"),
    MUSIC("음악"),
    ETC("기타");

    private final String displayName;
}