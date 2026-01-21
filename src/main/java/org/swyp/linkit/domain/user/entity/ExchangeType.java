package org.swyp.linkit.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExchangeType {

    ONLINE("온라인"),
    OFFLINE("오프라인"),
    BOTH("둘다");

    private final String description;
}