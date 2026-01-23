package org.swyp.linkit.domain.notification.service;

import org.swyp.linkit.domain.notification.dto.NotificationDto;
import org.swyp.linkit.domain.notification.dto.response.ChatRoomUnreadCountResponseDto;
import org.swyp.linkit.domain.notification.dto.response.NotificationListResponseDto;
import org.swyp.linkit.domain.notification.dto.response.UnreadCountResponseDto;
import org.swyp.linkit.domain.notification.entity.NotificationType;

public interface NotificationService {

    // ===== 알림 생성 =====

    /**
     * 알림 생성 (발신자 있음)
     */
    NotificationDto createNotification(Long receiverId, Long senderId, NotificationType type, Long refId);

    /**
     * 시스템 알림 생성 (발신자 없음)
     */
    NotificationDto createSystemNotification(Long receiverId, NotificationType type, Long refId);

    // ===== 미읽음 개수 조회 =====

    /**
     * 탭별 미읽음 알림 개수 조회
     * - 요청 관리 탭: REQUEST_RECEIVED + REQUEST_SENT + REQUEST_STATUS_CHANGED
     * - 받은 요청: REQUEST_RECEIVED
     * - 보낸 요청: REQUEST_SENT + REQUEST_STATUS_CHANGED
     * - 메시지 탭: CHAT_MESSAGE
     */
    UnreadCountResponseDto getUnreadCounts(Long userId);

    /**
     * 특정 채팅방의 미읽음 알림 개수 조회
     */
    ChatRoomUnreadCountResponseDto getChatRoomUnreadCount(Long userId, Long chatRoomId);

    // ===== 알림 목록 조회 =====

    /**
     * 전체 알림 목록 조회
     */
    NotificationListResponseDto getNotifications(Long userId);

    // ===== 알림 읽음 처리 =====

    /**
     * 요청 관리 페이지 진입 - 모든 요청 관련 알림 읽음 처리
     */
    int markRequestNotificationsAsRead(Long userId);

    /**
     * 받은 요청 탭 진입 - REQUEST_RECEIVED 알림 읽음 처리
     */
    int markReceivedRequestAsRead(Long userId);

    /**
     * 보낸 요청 탭 진입 - REQUEST_SENT, REQUEST_STATUS_CHANGED 알림 읽음 처리
     */
    int markSentRequestAsRead(Long userId);

    /**
     * 메시지 목록 페이지 진입 - 모든 CHAT_MESSAGE 알림 읽음 처리
     */
    int markMessageNotificationsAsRead(Long userId);

    /**
     * 특정 채팅방 진입 - 해당 채팅방의 CHAT_MESSAGE 알림 읽음 처리
     */
    int markChatRoomAsRead(Long userId, Long chatRoomId);

    /**
     * 단일 알림 읽음 처리
     */
    void markAsRead(Long userId, Long notificationId);

    /**
     * 전체 알림 읽음 처리
     */
    int markAllAsRead(Long userId);
}
