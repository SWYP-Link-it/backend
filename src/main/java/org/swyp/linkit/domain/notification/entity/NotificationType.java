package org.swyp.linkit.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    REQUEST_RECEIVED("요청 수신"),
    REQUEST_SENT("요청 발신"),
    /** @deprecated 신규 코드에서는 SENT_REQUEST_STATUS_CHANGED / RECEIVED_REQUEST_STATUS_CHANGED 사용 */
    @Deprecated
    REQUEST_STATUS_CHANGED("요청 상태 변경"),
    /** 보낸 요청 상태 변경 — 수락/거절/수신자 취소 시 요청자에게 발송 */
    SENT_REQUEST_STATUS_CHANGED("보낸 요청 상태 변경"),
    /** 받은 요청 상태 변경 — 요청자 취소/만료 시 수신자에게 발송 */
    RECEIVED_REQUEST_STATUS_CHANGED("받은 요청 상태 변경"),
    CHAT_MESSAGE("채팅 메시지");

    private final String description;
}