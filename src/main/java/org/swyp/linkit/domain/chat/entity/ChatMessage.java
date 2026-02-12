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

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Column(name = "file_url")
    private String fileUrl;

    @Builder(access = AccessLevel.PRIVATE)
    private ChatMessage(ChatRoom chatRoom, SenderRole senderRole, String content,
                        MessageType messageType, String fileUrl) {
        this.chatRoom = chatRoom;
        this.senderRole = senderRole;
        this.content = content != null ? content : "";
        this.messageType = messageType != null ? messageType : MessageType.TEXT;
        this.fileUrl = fileUrl;
    }

    public static ChatMessage create(ChatRoom chatRoom, User sender, SenderRole senderRole, String content) {
        return create(chatRoom, sender, senderRole, content, MessageType.TEXT, null);
    }

    public static ChatMessage create(ChatRoom chatRoom, User sender, SenderRole senderRole, String content,
                                     MessageType messageType, String fileUrl) {
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderRole(senderRole)
                .content(content)
                .messageType(messageType)
                .fileUrl(fileUrl)
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
