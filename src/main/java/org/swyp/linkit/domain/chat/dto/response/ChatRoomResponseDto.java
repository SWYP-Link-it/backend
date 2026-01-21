package org.swyp.linkit.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.chat.dto.ChatRoomDto;
import org.swyp.linkit.domain.chat.entity.ChatRoomStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "채팅방 정보")
public class ChatRoomResponseDto {

    @Schema(description = "채팅방 ID", example = "1")
    private Long roomId;

    @Schema(description = "멘토 사용자 ID", example = "10")
    private Long mentorId;

    @Schema(description = "멘티 사용자 ID", example = "20")
    private Long menteeId;

    @Schema(description = "채팅방 상태", example = "ACTIVE")
    private ChatRoomStatus status;

    @Schema(description = "상대방 사용자 ID (목록 표시용)", example = "20")
    private Long partnerId;

    @Schema(description = "상대방 닉네임", example = "홍길동")
    private String partnerNickname;

    @Schema(description = "상대방 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String partnerProfileImageUrl;

    @Schema(description = "마지막 메시지 ID", example = "100")
    private Long lastMessageId;

    @Schema(description = "마지막 메시지 내용", example = "안녕하세요!")
    private String lastMessageContent;

    @Schema(description = "마지막 메시지 시간 (epoch milliseconds)", example = "1705651200000")
    private Long lastMessageAtEpochMs;

    @Schema(description = "현재 사용자 기준 읽지 않은 메시지 수", example = "3")
    private Integer unreadCount;

    @Schema(description = "멘토가 읽지 않은 메시지 수", example = "2")
    private Integer unreadMentorCount;

    @Schema(description = "멘티가 읽지 않은 메시지 수", example = "1")
    private Integer unreadMenteeCount;

    @Schema(description = "채팅방 생성 시간 (epoch milliseconds)", example = "1705564800000")
    private Long createdAtEpochMs;

    @Schema(description = "채팅방 수정 시간 (epoch milliseconds)", example = "1705651200000")
    private Long modifiedAtEpochMs;

    public static ChatRoomResponseDto from(ChatRoomDto dto) {
        return ChatRoomResponseDto.builder()
                .roomId(dto.getRoomId())
                .mentorId(dto.getMentorId())
                .menteeId(dto.getMenteeId())
                .status(dto.getStatus())
                .partnerId(dto.getPartnerId())
                .partnerNickname(dto.getPartnerNickname())
                .partnerProfileImageUrl(dto.getPartnerProfileImageUrl())
                .lastMessageId(dto.getLastMessageId())
                .lastMessageContent(dto.getLastMessageContent())
                .lastMessageAtEpochMs(dto.getLastMessageAtEpochMs())
                .unreadCount(dto.getUnreadCount())
                .unreadMentorCount(dto.getUnreadMentorCount())
                .unreadMenteeCount(dto.getUnreadMenteeCount())
                .createdAtEpochMs(dto.getCreatedAtEpochMs())
                .modifiedAtEpochMs(dto.getModifiedAtEpochMs())
                .build();
    }
}