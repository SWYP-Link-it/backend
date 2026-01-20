package org.swyp.linkit.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "채팅 메시지 전송 요청")
public class ChatSendRequestDto {

    @NotNull(message = "채팅방 ID는 필수입니다.")
    @Schema(description = "채팅방 ID", example = "1")
    private Long roomId;

    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Schema(description = "전송할 메시지 내용", example = "안녕하세요!")
    private String text;
}