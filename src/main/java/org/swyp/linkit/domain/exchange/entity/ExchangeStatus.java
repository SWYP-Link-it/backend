package org.swyp.linkit.domain.exchange.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExchangeStatus {

    // 요청 관리 내역에는
    // 표시만 가져간다.
    PENDING("대기중"), //이녀석
    ACCEPTED("수락됨"), //이녀석
    REJECTED("거절됨"), //이녀석
    CANCELED("취소됨"), //이녀석
    EXPIRED("거절됨"), // 거절됨 처리 해야됨
    COMPLETED("완료됨"),
    SETTLED("정산 완료됨");

    private final String description;
}
