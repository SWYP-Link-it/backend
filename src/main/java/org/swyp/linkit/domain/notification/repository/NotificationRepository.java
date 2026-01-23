package org.swyp.linkit.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.notification.entity.Notification;
import org.swyp.linkit.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 특정 사용자의 미읽음 알림 개수 조회 (전체)
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver.id = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 미읽음 알림 개수 조회 (알림 타입별)
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver.id = :userId AND n.isRead = false AND n.notificationType = :type")
    long countUnreadByUserIdAndType(@Param("userId") Long userId, @Param("type") NotificationType type);

    /**
     * 특정 사용자의 미읽음 알림 개수 조회 (여러 알림 타입)
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver.id = :userId AND n.isRead = false AND n.notificationType IN :types")
    long countUnreadByUserIdAndTypes(@Param("userId") Long userId, @Param("types") List<NotificationType> types);

    /**
     * 특정 사용자의 미읽음 채팅 알림 개수 조회 (채팅방별)
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver.id = :userId AND n.isRead = false " +
            "AND n.notificationType = 'CHAT_MESSAGE' AND n.refId = :chatRoomId")
    long countUnreadChatByUserIdAndRoomId(@Param("userId") Long userId, @Param("chatRoomId") Long chatRoomId);

    /**
     * 특정 사용자의 알림 목록 조회 (최신순)
     */
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    /**
     * 특정 사용자의 미읽음 알림 목록 조회
     */
    List<Notification> findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(Long receiverId);

    /**
     * 특정 사용자의 읽은 알림 중 특정 날짜 이후 생성된 것만 조회 (최신순)
     */
    @Query("SELECT n FROM Notification n WHERE n.receiver.id = :receiverId AND n.isRead = true " +
            "AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findReadByReceiverIdAndCreatedAtAfter(
            @Param("receiverId") Long receiverId,
            @Param("since") LocalDateTime since);

    /**
     * 특정 타입의 미읽음 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.receiver.id = :userId AND n.isRead = false AND n.notificationType = :type")
    List<Notification> findUnreadByUserIdAndType(@Param("userId") Long userId, @Param("type") NotificationType type);

    /**
     * 여러 타입의 미읽음 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.receiver.id = :userId AND n.isRead = false AND n.notificationType IN :types")
    List<Notification> findUnreadByUserIdAndTypes(@Param("userId") Long userId, @Param("types") List<NotificationType> types);

    /**
     * 특정 채팅방의 미읽음 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.receiver.id = :userId AND n.isRead = false " +
            "AND n.notificationType = 'CHAT_MESSAGE' AND n.refId = :chatRoomId")
    List<Notification> findUnreadChatByUserIdAndRoomId(@Param("userId") Long userId, @Param("chatRoomId") Long chatRoomId);

    /**
     * 특정 타입의 알림 일괄 읽음 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :userId AND n.isRead = false AND n.notificationType = :type")
    int markAsReadByUserIdAndType(@Param("userId") Long userId, @Param("type") NotificationType type);

    /**
     * 여러 타입의 알림 일괄 읽음 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :userId AND n.isRead = false AND n.notificationType IN :types")
    int markAsReadByUserIdAndTypes(@Param("userId") Long userId, @Param("types") List<NotificationType> types);

    /**
     * 특정 채팅방의 알림 일괄 읽음 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :userId AND n.isRead = false " +
            "AND n.notificationType = 'CHAT_MESSAGE' AND n.refId = :chatRoomId")
    int markChatAsReadByUserIdAndRoomId(@Param("userId") Long userId, @Param("chatRoomId") Long chatRoomId);

    /**
     * 모든 알림 일괄 읽음 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);
}