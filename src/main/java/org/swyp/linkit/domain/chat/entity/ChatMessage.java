package org.swyp.linkit.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false)
    private SenderRole senderRole;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder(access = AccessLevel.PRIVATE)
    private ChatMessage(ChatRoom chatRoom, SenderRole senderRole, String content) {
        this.chatRoom = chatRoom;
        this.senderRole = senderRole;
        this.content = content;
    }

    /**
     * 채팅 메시지 생성
     */
    public static ChatMessage create(ChatRoom chatRoom, User sender, SenderRole senderRole, String content) {
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderRole(senderRole)
                .content(content)
                .build();
        message.assignSender(sender);
        return message;
    }

    // === 연관관계 편의 메서드 ===
    private void assignSender(User sender) {
        this.sender = sender;
    }

    // === 편의 메서드: ID 조회 (하위 호환성) ===
    public Long getSenderId() {
        return sender != null ? sender.getId() : null;
    }
}
