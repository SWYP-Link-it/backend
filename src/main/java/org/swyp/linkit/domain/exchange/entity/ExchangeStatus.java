package org.swyp.linkit.domain.exchange.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExchangeStatus {

    PENDING("대기"),
    ACCEPTED("수락"),
    REJECTED("거절"),
    PROCESSING("진행중"),
    COMPLETED("완료"),
    EXPIRED("만료"),
    CANCELED("취소"),
    SETTLED("정산 완료");

    private final String description;
}
