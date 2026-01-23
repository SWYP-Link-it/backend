package org.swyp.linkit.domain.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "sender_id")
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Notification(User receiver, Long senderId, NotificationType notificationType,
                         Long refId, boolean isRead) {
        this.receiver = receiver;
        this.senderId = senderId;
        this.notificationType = notificationType;
        this.refId = refId;
        this.isRead = isRead;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 알림 생성 (발신자 있음)
     */
    public static Notification create(User receiver, Long senderId,
                                       NotificationType notificationType, Long refId) {
        return Notification.builder()
                .receiver(receiver)
                .senderId(senderId)
                .notificationType(notificationType)
                .refId(refId)
                .isRead(false)
                .build();
    }

    /**
     * 시스템 알림 생성 (발신자 없음)
     */
    public static Notification createSystemNotification(User receiver,
                                                         NotificationType notificationType, Long refId) {
        return Notification.builder()
                .receiver(receiver)
                .senderId(null)
                .notificationType(notificationType)
                .refId(refId)
                .isRead(false)
                .build();
    }

    /**
     * 알림 읽음 처리
     */
    public void markAsRead() {
        this.isRead = true;
    }
}