package org.swyp.linkit.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SkillProficiency {

    HIGH("상"),
    MEDIUM("중"),
    LOW("하");

    private final String description;
}
