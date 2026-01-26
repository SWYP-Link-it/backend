package org.swyp.linkit.domain.credit.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SupplyType {

    USE("사용"),
    ADD("수급");

    private final String description;
}