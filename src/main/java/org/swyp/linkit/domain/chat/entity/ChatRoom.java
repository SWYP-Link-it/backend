package org.swyp.linkit.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.global.common.domain.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mentor_id", nullable = false)
    private Long mentorId;

    @Column(name = "mentee_id", nullable = false)
    private Long menteeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomStatus status;

    @Column(name = "last_message_id")
    private Long lastMessageId;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "unread_mentor_count", nullable = false)
    private Integer unreadMentorCount;

    @Column(name = "unread_mentee_count", nullable = false)
    private Integer unreadMenteeCount;

    @Builder(access = AccessLevel.PRIVATE)
    private ChatRoom(Long mentorId, Long menteeId, ChatRoomStatus status,
                     Integer unreadMentorCount, Integer unreadMenteeCount) {
        this.mentorId = mentorId;
        this.menteeId = menteeId;
        this.status = status;
        this.unreadMentorCount = unreadMentorCount;
        this.unreadMenteeCount = unreadMenteeCount;
    }

    /**
     * 1:1 채팅방 생성 (멘토-멘티)
     */
    public static ChatRoom create(Long mentorId, Long menteeId) {
        return ChatRoom.builder()
                .mentorId(mentorId)
                .menteeId(menteeId)
                .status(ChatRoomStatus.OPEN)
                .unreadMentorCount(0)
                .unreadMenteeCount(0)
                .build();
    }

    /**
     * 마지막 메시지 정보 업데이트
     */
    public void updateLastMessage(Long messageId, LocalDateTime messageAt) {
        this.lastMessageId = messageId;
        this.lastMessageAt = messageAt;
    }

    /**
     * 채팅방 상태 변경
     */
    public void changeStatus(ChatRoomStatus status) {
        this.status = status;
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