package org.swyp.linkit.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.chat.entity.MessageType;

@Getter
@NoArgsConstructor
@Schema(description = "채팅 메시지 전송 요청")
public class ChatSendRequestDto {

    @NotNull(message = "채팅방 ID는 필수입니다.")
    @Schema(description = "채팅방 ID", example = "1")
    private Long roomId;

    @Schema(description = "전송할 메시지 내용", example = "안녕하세요!")
    private String text;

    @Schema(description = "메시지 타입 (TEXT, IMAGE)", example = "TEXT", defaultValue = "TEXT")
    private MessageType messageType;

    @Schema(description = "이미지 URL (IMAGE 타입일 때 필수)", example = "https://kr.object.ncloudstorage.com/bucket/chat-images/1/uuid.png")
    private String imageUrl;

    public MessageType getMessageType() {
        return messageType != null ? messageType : MessageType.TEXT;
    }

    @AssertTrue(message = "TEXT 메시지는 내용이 비어있을 수 없습니다.")
    private boolean isTextValid() {
        if (getMessageType() == MessageType.TEXT) {
            return text != null && !text.isBlank();
        }
        return true;
    }

    @AssertTrue(message = "IMAGE 메시지는 이미지 URL이 비어있을 수 없습니다.")
    private boolean isImageUrlValid() {
        if (getMessageType() == MessageType.IMAGE) {
            return imageUrl != null && !imageUrl.isBlank();
        }
        return true;
    }
}