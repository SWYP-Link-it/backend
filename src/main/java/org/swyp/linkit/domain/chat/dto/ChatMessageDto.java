package org.swyp.linkit.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.chat.entity.ChatMessage;
import org.swyp.linkit.domain.chat.entity.SenderRole;

import java.time.ZoneOffset;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {

    private Long messageId;
    private Long roomId;
    private Long senderId;
    private SenderRole senderRole;
    private String content;
    private Long createdAtEpochMs;
    private Boolean isMine;

    public static ChatMessageDto from(ChatMessage message) {
        return ChatMessageDto.builder()
                .messageId(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSenderId())
                .senderRole(message.getSenderRole())
                .content(message.getContent())
                .createdAtEpochMs(message.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();
    }

    public static ChatMessageDto from(ChatMessage message, Long currentUserId) {
        return ChatMessageDto.builder()
                .messageId(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSenderId())
                .senderRole(message.getSenderRole())
                .content(message.getContent())
                .createdAtEpochMs(message.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .isMine(message.getSenderId().equals(currentUserId))
                .build();
    }
}