package org.swyp.linkit.domain.notification.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.notification.entity.Notification;
import org.swyp.linkit.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * WebSocket으로 전송되는 알림 메시지 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class NotificationMessageDto {

    private Long notificationId;
    private Long receiverId;
    private Long senderId;
    private String senderNickname;
    private NotificationType notificationType;
    private Long refId;
    private String message;
    private long createdAtEpochMs;

    public static NotificationMessageDto from(Notification notification, String senderNickname, String message) {
        return NotificationMessageDto.builder()
                .notificationId(notification.getId())
                .receiverId(notification.getReceiver().getId())
                .senderId(notification.getSenderId())
                .senderNickname(senderNickname)
                .notificationType(notification.getNotificationType())
                .refId(notification.getRefId())
                .message(message)
                .createdAtEpochMs(notification.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();
    }

    public static NotificationMessageDto of(Long receiverId, Long senderId, String senderNickname,
                                             NotificationType type, Long refId, String message) {
        return NotificationMessageDto.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .senderNickname(senderNickname)
                .notificationType(type)
                .refId(refId)
                .message(message)
                .createdAtEpochMs(System.currentTimeMillis())
                .build();
    }
}