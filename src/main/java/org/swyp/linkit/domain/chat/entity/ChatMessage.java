package org.swyp.linkit.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false)
    private SenderRole senderRole;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder(access = AccessLevel.PRIVATE)
    private ChatMessage(ChatRoom chatRoom, Long senderId, SenderRole senderRole, String content) {
        this.chatRoom = chatRoom;
        this.senderId = senderId;
        this.senderRole = senderRole;
        this.content = content;
    }

    /**
     * 채팅 메시지 생성
     */
    public static ChatMessage create(ChatRoom chatRoom, Long senderId, SenderRole senderRole, String content) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderId(senderId)
                .senderRole(senderRole)
                .content(content)
                .build();
    }
}
