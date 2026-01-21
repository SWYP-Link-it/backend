package org.swyp.linkit.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PreferredRegion {
    SEOUL("서울"),
    GYEONGGI("경기도"),
    GANGWON("강원도"),
    CHUNGCHEONG("충청도"),
    GYEONGSANG("경상도"),
    JEOLLA("전라도"),
    JEJU("제주도");

    private final String description;
}