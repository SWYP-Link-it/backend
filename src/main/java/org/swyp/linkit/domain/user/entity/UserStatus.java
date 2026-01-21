package org.swyp.linkit.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    PROFILE_PENDING("프로필 미작성"),
    ACTIVE("활성"),
    WITHDRAWN("탈퇴");

    private final String description;
}