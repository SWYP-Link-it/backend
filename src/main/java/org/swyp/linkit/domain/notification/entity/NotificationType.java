package org.swyp.linkit.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    REQUEST_RECEIVED("요청 수신"),
    REQUEST_SENT("요청 발신"),
    REQUEST_STATUS_CHANGED("요청 상태 변경"),
    CHAT_MESSAGE("채팅 메시지");

    private final String description;
}