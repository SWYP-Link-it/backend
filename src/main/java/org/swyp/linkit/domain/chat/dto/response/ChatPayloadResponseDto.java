package org.swyp.linkit.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "WebSocket 채팅 메시지 페이로드")
public class ChatPayloadResponseDto {

    @Schema(description = "채팅방 ID", example = "1")
    private Long roomId;

    @Schema(description = "메시지 ID", example = "100")
    private Long messageId;

    @Schema(description = "발신자 사용자 ID", example = "10")
    private Long senderId;

    @Schema(description = "발신자 역할 (MENTOR 또는 MENTEE)", example = "MENTOR")
    private String senderRole;

    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String text;

    @Schema(description = "메시지 전송 시간 (epoch milliseconds)", example = "1705651200000")
    private Long sentAtEpochMs;

    @Schema(description = "시스템 메시지 여부", example = "false")
    private boolean system;

    @Schema(description = "읽음 처리된 마지막 메시지 ID (읽음 이벤트용)", example = "99")
    private Long readUpToMessageId;

    @Schema(description = "읽음 처리한 사용자 ID (읽음 이벤트용)", example = "20")
    private Long readerId;
}