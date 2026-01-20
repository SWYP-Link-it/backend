package org.swyp.linkit.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SkillLevel {

    HIGH("상", 3),
    MEDIUM("중", 2),
    LOW("하", 1);

    private final String displayName;
    private final int priority;
}