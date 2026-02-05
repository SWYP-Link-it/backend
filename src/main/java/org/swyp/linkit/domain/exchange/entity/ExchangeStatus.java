package org.swyp.linkit.domain.exchange.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExchangeStatus {

    PENDING("대기중"),
    ACCEPTED("수락됨"),
    REJECTED("거절됨"),
    CANCELED("취소됨"),
    EXPIRED("거절됨"),
    COMPLETED("완료됨"),
    SETTLED("정산 완료됨");

    private final String description;
}
