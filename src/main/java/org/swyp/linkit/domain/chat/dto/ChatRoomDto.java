package org.swyp.linkit.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.chat.entity.ChatRoom;
import org.swyp.linkit.domain.chat.entity.ChatRoomStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDto {

    private Long roomId;
    private Long mentorId;
    private Long menteeId;
    private ChatRoomStatus status;
    private Long partnerId;
    private String partnerNickname;
    private String partnerProfileImageUrl;
    private Long lastMessageId;
    private String lastMessageContent;
    private Long lastMessageAtEpochMs;
    private Integer unreadCount;
    private Integer unreadMentorCount;
    private Integer unreadMenteeCount;
    private Long createdAtEpochMs;
    private Long modifiedAtEpochMs;

    public static ChatRoomDto from(ChatRoom room) {
        return ChatRoomDto.builder()
                .roomId(room.getId())
                .mentorId(room.getMentorId())
                .menteeId(room.getMenteeId())
                .status(room.getStatus())
                .lastMessageId(room.getLastMessageId())
                .lastMessageAtEpochMs(toEpochMs(room.getLastMessageAt()))
                .unreadMentorCount(room.getUnreadMentorCount())
                .unreadMenteeCount(room.getUnreadMenteeCount())
                .createdAtEpochMs(toEpochMs(room.getCreatedAt()))
                .modifiedAtEpochMs(toEpochMs(room.getModifiedAt()))
                .build();
    }

    public static ChatRoomDto fromWithPartner(ChatRoom room, Long currentUserId,
                                              String partnerNickname, String partnerProfileImageUrl,
                                              String lastMessageContent) {
        boolean isMentor = room.getMentorId().equals(currentUserId);
        Long partnerId = isMentor ? room.getMenteeId() : room.getMentorId();
        Integer unreadCount = isMentor ? room.getUnreadMentorCount() : room.getUnreadMenteeCount();

        return ChatRoomDto.builder()
                .roomId(room.getId())
                .mentorId(room.getMentorId())
                .menteeId(room.getMenteeId())
                .status(room.getStatus())
                .partnerId(partnerId)
                .partnerNickname(partnerNickname)
                .partnerProfileImageUrl(partnerProfileImageUrl)
                .lastMessageId(room.getLastMessageId())
                .lastMessageContent(lastMessageContent)
                .lastMessageAtEpochMs(toEpochMs(room.getLastMessageAt()))
                .unreadCount(unreadCount)
                .unreadMentorCount(room.getUnreadMentorCount())
                .unreadMenteeCount(room.getUnreadMenteeCount())
                .createdAtEpochMs(toEpochMs(room.getCreatedAt()))
                .modifiedAtEpochMs(toEpochMs(room.getModifiedAt()))
                .build();
    }

    private static Long toEpochMs(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toInstant(ZoneOffset.UTC).toEpochMilli() : null;
    }
}