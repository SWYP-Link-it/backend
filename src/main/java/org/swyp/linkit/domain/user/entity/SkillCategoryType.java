package org.swyp.linkit.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SkillCategoryType {

    DEVELOPMENT("개발"),
    DESIGN("디자인"),
    EDITING("편집"),
    MARKETING("마케팅"),
    LANGUAGE("외국어"),
    FINANCE("재테크"),
    SPORTS("운동"),
    MUSIC("음악"),
    ETC("기타");

    private final String displayName;
}