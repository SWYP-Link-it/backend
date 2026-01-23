package org.swyp.linkit.domain.notification.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ChatRoomUnreadCountResponseDto {

    private Long chatRoomId;
    private long unreadCount;

    public static ChatRoomUnreadCountResponseDto of(Long chatRoomId, long unreadCount) {
        return ChatRoomUnreadCountResponseDto.builder()
                .chatRoomId(chatRoomId)
                .unreadCount(unreadCount)
                .build();
    }
}