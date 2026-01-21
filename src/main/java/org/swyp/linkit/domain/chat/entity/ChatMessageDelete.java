package org.swyp.linkit.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message_delete", indexes = {
    @Index(name = "idx_user_deleted", columnList = "user_id, deleted_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageDelete extends BaseTimeEntity {

    @EmbeddedId
    private ChatMessageDeleteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chatMessageId")
    @JoinColumn(name = "chat_message_id")
    private ChatMessage chatMessage;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private ChatMessageDelete(ChatMessageDeleteId id, ChatMessage chatMessage) {
        this.id = id;
        this.chatMessage = chatMessage;
    }

    @PrePersist
    public void prePersist() {
        if (deletedAt == null) {
            deletedAt = LocalDateTime.now();
        }
    }

    /**
     * 메시지 삭제 기록 생성
     */
    public static ChatMessageDelete create(ChatMessage chatMessage, Long userId) {
        return ChatMessageDelete.builder()
                .id(new ChatMessageDeleteId(chatMessage.getId(), userId))
                .chatMessage(chatMessage)
                .build();
    }
}
