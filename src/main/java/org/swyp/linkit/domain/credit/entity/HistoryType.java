package org.swyp.linkit.domain.credit.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HistoryType {
    SIGNUP_REWARD("회원가입 리워드"),
    PROFILE_REWARD("프로필 작성 리워드"),
    EVENT_REWARD("이벤트 리워드"),
    EXCHANGE_REQUEST("스킬 교환 요청"),
    EXCHANGE_REJECTED("스킬 교환 요청 거절"),
    EXCHANGE_EXPIRED("스킬 교환 요청 만료"),
    EXCHANGE_SETTLED("스킬 교환 요청 정산");

    private final String description;
}
