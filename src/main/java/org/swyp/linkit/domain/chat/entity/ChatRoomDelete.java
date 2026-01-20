package org.swyp.linkit.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_delete")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomDelete extends BaseTimeEntity {

    @EmbeddedId
    private ChatRoomDeleteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chatRoomId")
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private ChatRoomDelete(ChatRoomDeleteId id, ChatRoom chatRoom) {
        this.id = id;
        this.chatRoom = chatRoom;
    }

    @PrePersist
    public void prePersist() {
        if (deletedAt == null) {
            deletedAt = LocalDateTime.now();
        }
    }

    /**
     * 채팅방 삭제 기록 생성
     */
    public static ChatRoomDelete create(ChatRoom chatRoom, Long userId) {
        return ChatRoomDelete.builder()
                .id(new ChatRoomDeleteId(chatRoom.getId(), userId))
                .chatRoom(chatRoom)
                .build();
    }
}
