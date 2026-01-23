package org.swyp.linkit.domain.notification.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.notification.dto.NotificationDto;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class NotificationListResponseDto {

    private List<NotificationDto> notifications;
    private long totalCount;
    private long unreadCount;

    public static NotificationListResponseDto of(List<NotificationDto> notifications, long unreadCount) {
        return NotificationListResponseDto.builder()
                .notifications(notifications)
                .totalCount(notifications.size())
                .unreadCount(unreadCount)
                .build();
    }
}