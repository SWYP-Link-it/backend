package org.swyp.linkit.domain.notification.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.notification.entity.Notification;
import org.swyp.linkit.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class NotificationDto {

    private Long id;
    private Long receiverId;
    private Long senderId;
    private NotificationType notificationType;
    private Long refId;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationDto from(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .receiverId(notification.getReceiver().getId())
                .senderId(notification.getSenderId())
                .notificationType(notification.getNotificationType())
                .refId(notification.getRefId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}