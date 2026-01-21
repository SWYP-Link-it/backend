package org.swyp.linkit.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.chat.dto.ChatMessageDto;
import org.swyp.linkit.domain.chat.entity.SenderRole;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "채팅 메시지 정보")
public class ChatMessageResponseDto {

    @Schema(description = "메시지 ID", example = "1")
    private Long messageId;

    @Schema(description = "채팅방 ID", example = "10")
    private Long roomId;

    @Schema(description = "발신자 사용자 ID", example = "100")
    private Long senderId;

    @Schema(description = "발신자 역할 (MENTOR 또는 MENTEE)", example = "MENTOR")
    private SenderRole senderRole;

    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String content;

    @Schema(description = "메시지 생성 시간 (epoch milliseconds)", example = "1705651200000")
    private Long createdAtEpochMs;

    @Schema(description = "본인이 보낸 메시지 여부", example = "true")
    private Boolean isMine;

    public static ChatMessageResponseDto from(ChatMessageDto dto) {
        return ChatMessageResponseDto.builder()
                .messageId(dto.getMessageId())
                .roomId(dto.getRoomId())
                .senderId(dto.getSenderId())
                .senderRole(dto.getSenderRole())
                .content(dto.getContent())
                .createdAtEpochMs(dto.getCreatedAtEpochMs())
                .isMine(dto.getIsMine())
                .build();
    }
}