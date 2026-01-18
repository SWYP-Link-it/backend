package org.swyp.linkit.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message_delete", indexes = {
    @Index(name = "idx_user_deleted", columnList = "user_id, deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDelete {

    @EmbeddedId
    private ChatMessageDeleteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chatMessageId")
    @JoinColumn(name = "chat_message_id")
    private ChatMessage chatMessage;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        if (deletedAt == null) deletedAt = LocalDateTime.now();
    }
}