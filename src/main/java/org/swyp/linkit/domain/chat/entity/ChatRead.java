package org.swyp.linkit.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_read")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRead extends BaseTimeEntity {

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

    @Builder(access = AccessLevel.PRIVATE)
    private ChatRead(ChatReadId id, ChatRoom chatRoom, Long lastReadMessageId) {
        this.id = id;
        this.chatRoom = chatRoom;
        this.lastReadMessageId = lastReadMessageId;
    }

    @PrePersist
    @PreUpdate
    public void prePersistAndUpdate() {
        modifiedAt = LocalDateTime.now();
    }

    /**
     * 읽음 기록 생성
     */
    public static ChatRead create(ChatRoom chatRoom, Long userId, Long lastReadMessageId) {
        return ChatRead.builder()
                .id(new ChatReadId(chatRoom.getId(), userId))
                .chatRoom(chatRoom)
                .lastReadMessageId(lastReadMessageId)
                .build();
    }

    /**
     * 마지막 읽은 메시지 업데이트
     */
    public void updateLastReadMessage(Long messageId) {
        this.lastReadMessageId = messageId;
        this.modifiedAt = LocalDateTime.now();
    }
}
