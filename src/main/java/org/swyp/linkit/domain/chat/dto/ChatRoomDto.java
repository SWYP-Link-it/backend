package org.swyp.linkit.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.chat.entity.ChatRoom;
import org.swyp.linkit.domain.chat.entity.ChatRoomStatus;
import org.swyp.linkit.domain.user.entity.User;

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
    private Boolean isMentor;          // 현재 사용자가 멘토인지 여부
    private Long partnerId;
    private String partnerNickname;
    private String partnerProfileImageUrl;
    private Long lastMessageId;
    private String lastMessageContent;
    private Long lastMessageAtEpochMs;
    private long unreadCount;
    private Long createdAtEpochMs;
    private Long modifiedAtEpochMs;

    /**
     * ChatRoom 엔티티 → DTO 변환 (현재 사용자 기준, 상대방 정보 포함)
     * 연관관계가 fetch join 되어 있어야 N+1 없이 User 정보 접근 가능
     */
    public static ChatRoomDto fromWithCurrentUser(ChatRoom room, Long currentUserId,
                                                  String lastMessageContent, long unreadCount) {
        boolean isMentor = room.getMentorId().equals(currentUserId);
        User partner = isMentor ? room.getMentee() : room.getMentor();

        return ChatRoomDto.builder()
                .roomId(room.getId())
                .mentorId(room.getMentorId())
                .menteeId(room.getMenteeId())
                .status(room.getStatus())
                .isMentor(isMentor)
                .partnerId(partner != null ? partner.getId() : null)
                .partnerNickname(partner != null ? partner.getNickname() : "알 수 없음")
                .partnerProfileImageUrl(partner != null ? partner.getProfileImageUrl() : null)
                .lastMessageId(room.getLastMessageId())
                .lastMessageContent(lastMessageContent)
                .lastMessageAtEpochMs(toEpochMs(room.getLastMessageAt()))
                .unreadCount(unreadCount)
                .createdAtEpochMs(toEpochMs(room.getCreatedAt()))
                .modifiedAtEpochMs(toEpochMs(room.getModifiedAt()))
                .build();
    }

    /**
     * ChatRoom 엔티티 → DTO 변환 (파트너 정보 직접 전달)
     */
    public static ChatRoomDto fromWithPartner(ChatRoom room, Long currentUserId,
                                              String partnerNickname, String partnerProfileImageUrl,
                                              String lastMessageContent, long unreadCount) {
        boolean isMentor = room.getMentorId().equals(currentUserId);
        Long partnerId = isMentor ? room.getMenteeId() : room.getMentorId();

        return ChatRoomDto.builder()
                .roomId(room.getId())
                .mentorId(room.getMentorId())
                .menteeId(room.getMenteeId())
                .status(room.getStatus())
                .isMentor(isMentor)
                .partnerId(partnerId)
                .partnerNickname(partnerNickname)
                .partnerProfileImageUrl(partnerProfileImageUrl)
                .lastMessageId(room.getLastMessageId())
                .lastMessageContent(lastMessageContent)
                .lastMessageAtEpochMs(toEpochMs(room.getLastMessageAt()))
                .unreadCount(unreadCount)
                .createdAtEpochMs(toEpochMs(room.getCreatedAt()))
                .modifiedAtEpochMs(toEpochMs(room.getModifiedAt()))
                .build();
    }

    /**
     * LocalDateTime을 Unix epoch 밀리초로 변환합니다.
     * 모든 시간은 UTC 기준으로 저장 및 처리되며, 클라이언트에서 로컬 시간대로 변환하여 표시합니다.
     */
    private static Long toEpochMs(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toInstant(ZoneOffset.UTC).toEpochMilli() : null;
    }
}