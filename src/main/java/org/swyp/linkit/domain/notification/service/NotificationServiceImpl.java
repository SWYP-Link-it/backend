package org.swyp.linkit.domain.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.notification.dto.NotificationDto;
import org.swyp.linkit.domain.notification.dto.NotificationMessageDto;
import org.swyp.linkit.domain.notification.dto.response.ChatRoomUnreadCountResponseDto;
import org.swyp.linkit.domain.notification.dto.response.NotificationListResponseDto;
import org.swyp.linkit.domain.notification.dto.response.UnreadCountResponseDto;
import org.swyp.linkit.domain.notification.entity.Notification;
import org.swyp.linkit.domain.notification.entity.NotificationType;
import org.swyp.linkit.domain.notification.repository.NotificationRepository;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.error.exception.NotificationAccessDeniedException;
import org.swyp.linkit.global.error.exception.NotificationAlreadyReadException;
import org.swyp.linkit.global.error.exception.NotificationNotFoundException;
import org.swyp.linkit.global.error.exception.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String NOTIFICATION_CHANNEL_PREFIX = "notification:user:";

    // 요청 관련 알림 타입들
    private static final List<NotificationType> REQUEST_TYPES = List.of(
            NotificationType.REQUEST_RECEIVED,
            NotificationType.REQUEST_SENT,
            NotificationType.REQUEST_STATUS_CHANGED
    );

    // 보낸 요청 관련 알림 타입들
    private static final List<NotificationType> SENT_REQUEST_TYPES = List.of(
            NotificationType.REQUEST_SENT,
            NotificationType.REQUEST_STATUS_CHANGED
    );

    // ===== 알림 생성 + WebSocket 푸시 =====

    @Override
    @Transactional
    public NotificationDto createNotification(Long receiverId, Long senderId, NotificationType type, Long refId) {
        User receiver = findUserById(receiverId);
        User sender = senderId != null ? userRepository.findById(senderId).orElse(null) : null;

        Notification notification = Notification.create(receiver, senderId, type, refId);
        Notification savedNotification = notificationRepository.save(notification);

        // WebSocket 실시간 알림 발송
        String senderNickname = sender != null ? sender.getNickname() : "시스템";
        String message = generateNotificationMessage(type, senderNickname);
        publishNotificationToRedis(savedNotification, senderNickname, message);

        log.info("알림 생성 및 발송: receiverId={}, type={}, refId={}", receiverId, type, refId);
        return NotificationDto.from(savedNotification);
    }

    @Override
    @Transactional
    public NotificationDto createSystemNotification(Long receiverId, NotificationType type, Long refId) {
        User receiver = findUserById(receiverId);

        Notification notification = Notification.createSystemNotification(receiver, type, refId);
        Notification savedNotification = notificationRepository.save(notification);

        // WebSocket 실시간 알림 발송
        String message = generateNotificationMessage(type, "시스템");
        publishNotificationToRedis(savedNotification, "시스템", message);

        log.info("시스템 알림 생성 및 발송: receiverId={}, type={}, refId={}", receiverId, type, refId);
        return NotificationDto.from(savedNotification);
    }

    // ===== 미읽음 개수 조회 =====

    @Override
    public UnreadCountResponseDto getUnreadCounts(Long userId) {
        // 요청 관리 탭 - 전체 요청 관련 알림
        long requestTabCount = notificationRepository.countUnreadByUserIdAndTypes(userId, REQUEST_TYPES);

        // 받은 요청 탭
        long receivedRequestCount = notificationRepository.countUnreadByUserIdAndType(userId, NotificationType.REQUEST_RECEIVED);

        // 보낸 요청 탭 (보낸 요청 + 상태 변경)
        long sentRequestCount = notificationRepository.countUnreadByUserIdAndTypes(userId, SENT_REQUEST_TYPES);

        // 메시지 탭
        long messageTabCount = notificationRepository.countUnreadByUserIdAndType(userId, NotificationType.CHAT_MESSAGE);

        return UnreadCountResponseDto.of(requestTabCount, receivedRequestCount, sentRequestCount, messageTabCount);
    }

    @Override
    public ChatRoomUnreadCountResponseDto getChatRoomUnreadCount(Long userId, Long chatRoomId) {
        long unreadCount = notificationRepository.countUnreadChatByUserIdAndRoomId(userId, chatRoomId);
        return ChatRoomUnreadCountResponseDto.of(chatRoomId, unreadCount);
    }

    // ===== 알림 목록 조회 =====

    @Override
    public NotificationListResponseDto getNotifications(Long userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 안읽은 알림 전체 조회
        List<Notification> unreadNotifications = notificationRepository
                .findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        // 읽은 알림 중 7일 이내만 조회
        List<Notification> readNotifications = notificationRepository
                .findReadByReceiverIdAndCreatedAtAfter(userId, sevenDaysAgo);

        // 합치기 (안읽은 알림 먼저, 읽은 알림 나중에)
        List<Notification> combinedNotifications = new ArrayList<>();
        combinedNotifications.addAll(unreadNotifications);
        combinedNotifications.addAll(readNotifications);

        List<NotificationDto> notificationDtos = combinedNotifications.stream()
                .map(NotificationDto::from)
                .collect(Collectors.toList());

        return NotificationListResponseDto.of(notificationDtos, unreadNotifications.size());
    }

    // ===== 알림 읽음 처리 =====

    @Override
    @Transactional
    public int markRequestNotificationsAsRead(Long userId) {
        int count = notificationRepository.markAsReadByUserIdAndTypes(userId, REQUEST_TYPES);
        log.info("요청 알림 읽음 처리: userId={}, count={}", userId, count);
        return count;
    }

    @Override
    @Transactional
    public int markReceivedRequestAsRead(Long userId) {
        int count = notificationRepository.markAsReadByUserIdAndType(userId, NotificationType.REQUEST_RECEIVED);
        log.info("받은 요청 알림 읽음 처리: userId={}, count={}", userId, count);
        return count;
    }

    @Override
    @Transactional
    public int markSentRequestAsRead(Long userId) {
        int count = notificationRepository.markAsReadByUserIdAndTypes(userId, SENT_REQUEST_TYPES);
        log.info("보낸 요청 알림 읽음 처리: userId={}, count={}", userId, count);
        return count;
    }

    @Override
    @Transactional
    public int markMessageNotificationsAsRead(Long userId) {
        int count = notificationRepository.markAsReadByUserIdAndType(userId, NotificationType.CHAT_MESSAGE);
        log.info("메시지 알림 읽음 처리: userId={}, count={}", userId, count);
        return count;
    }

    @Override
    @Transactional
    public int markChatRoomAsRead(Long userId, Long chatRoomId) {
        int count = notificationRepository.markChatAsReadByUserIdAndRoomId(userId, chatRoomId);
        log.info("채팅방 알림 읽음 처리: userId={}, chatRoomId={}, count={}", userId, chatRoomId, count);
        return count;
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(NotificationNotFoundException::new);

        // 본인의 알림인지 검증
        if (!notification.getReceiver().getId().equals(userId)) {
            throw new NotificationAccessDeniedException();
        }

        // 이미 읽은 알림인지 확인
        if (notification.isRead()) {
            throw new NotificationAlreadyReadException();
        }

        notification.markAsRead();
        log.info("단일 알림 읽음 처리: userId={}, notificationId={}", userId, notificationId);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        int count = notificationRepository.markAllAsReadByUserId(userId);
        log.info("전체 알림 읽음 처리: userId={}, count={}", userId, count);
        return count;
    }

    // ===== Private Methods =====

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * Redis Pub/Sub을 통해 알림 발행
     */
    private void publishNotificationToRedis(Notification notification, String senderNickname, String message) {
        NotificationMessageDto payload = NotificationMessageDto.from(notification, senderNickname, message);

        try {
            String json = objectMapper.writeValueAsString(payload);
            String channel = NOTIFICATION_CHANNEL_PREFIX + notification.getReceiver().getId();
            redisTemplate.convertAndSend(channel, json);
            log.info("Redis 알림 발행: channel={}, notificationId={}", channel, notification.getId());
        } catch (JsonProcessingException e) {
            log.error("알림 직렬화 실패", e);
        }
    }

    /**
     * 알림 타입에 따른 메시지 생성
     */
    private String generateNotificationMessage(NotificationType type, String senderNickname) {
        return switch (type) {
            case REQUEST_RECEIVED -> senderNickname + "님이 스킬 교환을 요청했습니다.";
            case REQUEST_SENT -> senderNickname + "님에게 스킬 교환 요청을 보냈습니다.";
            case REQUEST_STATUS_CHANGED -> "스킬 교환 요청 상태가 변경되었습니다.";
            case CHAT_MESSAGE -> senderNickname + "님이 메시지를 보냈습니다.";
        };
    }
}