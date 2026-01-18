package org.swyp.linkit.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_read")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRead {

    @EmbeddedId
    private ChatReadId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chatRoomId")
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Column(name = "last_read_message_id", nullable = false)
    private Long lastReadMessageId;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @PrePersist
    @PreUpdate
    public void prePersistAndUpdate() {
        modifiedAt = LocalDateTime.now();
    }

    /**
     * 마지막 읽은 메시지 업데이트
     */
    public void updateLastReadMessage(Long messageId) {
        this.lastReadMessageId = messageId;
        this.modifiedAt = LocalDateTime.now();
    }
}