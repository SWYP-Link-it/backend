package org.swyp.linkit.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mentor_id", nullable = false)
    private Long mentorId;

    @Column(name = "mentee_id", nullable = false)
    private Long menteeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ChatRoomStatus status = ChatRoomStatus.OPEN;

    @Column(name = "last_message_id")
    private Long lastMessageId;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "unread_mentor_count", nullable = false)
    @Builder.Default
    private Integer unreadMentorCount = 0;

    @Column(name = "unread_mentee_count", nullable = false)
    @Builder.Default
    private Integer unreadMenteeCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (modifiedAt == null) modifiedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        modifiedAt = LocalDateTime.now();
    }

    /**
     * 마지막 메시지 정보 업데이트
     */
    public void updateLastMessage(Long messageId, LocalDateTime messageAt) {
        this.lastMessageId = messageId;
        this.lastMessageAt = messageAt;
    }

    /**
     * 멘토의 읽지 않은 메시지 수 증가
     */
    public void incrementUnreadMentorCount() {
        this.unreadMentorCount++;
    }

    /**
     * 멘티의 읽지 않은 메시지 수 증가
     */
    public void incrementUnreadMenteeCount() {
        this.unreadMenteeCount++;
    }

    /**
     * 멘토의 읽지 않은 메시지 수 초기화
     */
    public void resetUnreadMentorCount() {
        this.unreadMentorCount = 0;
    }

    /**
     * 멘티의 읽지 않은 메시지 수 초기화
     */
    public void resetUnreadMenteeCount() {
        this.unreadMenteeCount = 0;
    }
}