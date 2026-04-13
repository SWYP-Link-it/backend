package org.swyp.linkit.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.swyp.linkit.domain.chat.dto.ChatMessageDto;
import org.swyp.linkit.domain.chat.dto.response.ChatPayloadResponseDto;
import org.swyp.linkit.domain.chat.entity.*;
import org.swyp.linkit.domain.chat.repository.ChatMessageDeleteRepository;
import org.swyp.linkit.domain.chat.repository.ChatMessageRepository;
import org.swyp.linkit.domain.chat.repository.ChatReadRepository;
import org.swyp.linkit.domain.chat.repository.ChatRoomRepository;
import org.swyp.linkit.domain.notification.entity.NotificationType;
import org.swyp.linkit.domain.notification.service.NotificationService;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.error.exception.ChatInvalidMessageException;
import org.swyp.linkit.global.error.exception.ChatMessageNotFoundException;
import org.swyp.linkit.global.error.exception.ChatNotParticipantException;
import org.swyp.linkit.global.error.exception.ChatRoomNotFoundException;
import org.swyp.linkit.global.error.exception.UserNotFoundException;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatReadRepository chatReadRepository;
    private final ChatMessageDeleteRepository chatMessageDeleteRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    private static final String CHAT_CHANNEL_PREFIX = "chat:room:";

    /**
     * 사용자가 채팅방 참여자인지 확인
     */
    public void assertParticipant(Long userId, Long roomId) {
        // 디버깅: 채팅방 정보 조회
        ChatRoom room = chatRoomRepository.findByIdWithUsers(roomId).orElse(null);
        if (room != null) {
            log.debug("참여자 검증 - roomId={}, userId={}, mentorId={}, menteeId={}",
                    roomId, userId, room.getMentorId(), room.getMenteeId());
        } else {
            log.warn("참여자 검증 - 채팅방이 존재하지 않음: roomId={}", roomId);
        }

        boolean isParticipant = chatRoomRepository.existsByIdAndUserId(roomId, userId);
        if (!isParticipant) {
            log.warn("참여자 검증 실패 - roomId={}, userId={}", roomId, userId);
            throw new ChatNotParticipantException(roomId, userId);
        }
    }

    /**
     * 메시지 저장
     */
    @Transactional
    public ChatMessage saveMessage(Long roomId, Long senderId, String content) {
        return saveMessage(roomId, senderId, content, MessageType.TEXT, null);
    }

    @Transactional
    public ChatMessage saveMessage(Long roomId, Long senderId, String content,
                                   MessageType messageType, String fileUrl) {
        ChatRoom room = chatRoomRepository.findByIdWithUsers(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));

        User sender = findUserById(senderId);

        SenderRole senderRole = room.getMentorId().equals(senderId) ? SenderRole.MENTOR : SenderRole.MENTEE;

        ChatMessage message = ChatMessage.create(room, sender, senderRole, content, messageType, fileUrl);

        ChatMessage saved = chatMessageRepository.save(message);

        room.updateLastMessage(saved.getId(), saved.getCreatedAt());

        // 수신자에게 CHAT_MESSAGE 알림 생성 (Notification 기반 미읽음 카운트 관리)
        Long receiverId = senderRole == SenderRole.MENTOR ? room.getMenteeId() : room.getMentorId();
        notificationService.createNotification(receiverId, senderId, NotificationType.CHAT_MESSAGE, roomId);

        // 트랜잭션 커밋 후 Redis 발행 (Transactional Outbox 패턴)
        ChatPayloadResponseDto payload = ChatPayloadResponseDto.builder()
                .roomId(roomId)
                .messageId(saved.getId())
                .senderId(saved.getSenderId())
                .senderRole(saved.getSenderRole().name())
                .text(saved.getContent())
                .messageType(saved.getMessageType().name())
                .imageUrl(saved.getFileUrl())
                .sentAtEpochMs(saved.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .system(false)
                .build();
        Long savedMessageId = saved.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                doPublishToRedis(roomId, savedMessageId, payload);
            }
        });

        log.info("메시지 저장: roomId={}, senderId={}, messageId={}, type={}", roomId, senderId, saved.getId(), messageType);
        return saved;
    }

    /**
     * 채팅방 메시지 목록 조회 (삭제된 메시지 제외)
     */
    public List<ChatMessageDto> getMessages(Long roomId, Long userId) {
        assertParticipant(userId, roomId);

        // 사용자가 삭제한 메시지 ID 목록 조회
        List<Long> deletedMessageIds = chatMessageDeleteRepository.findDeletedMessageIdsByUserId(userId);

        // 메시지 조회 (생성순)
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);

        return messages.stream()
                .filter(m -> !deletedMessageIds.contains(m.getId()))
                .map(m -> ChatMessageDto.from(m, userId))
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 최근 메시지 조회 (페이징용)
     */
    public List<ChatMessageDto> getRecentMessages(Long roomId, Long userId, int limit) {
        assertParticipant(userId, roomId);

        List<Long> deletedMessageIds = chatMessageDeleteRepository.findDeletedMessageIdsByUserId(userId);
        List<ChatMessage> messages = chatMessageRepository.findTop50ByChatRoomIdOrderByIdDesc(roomId);

        return messages.stream()
                .filter(m -> !deletedMessageIds.contains(m.getId()))
                .limit(limit)
                .map(m -> ChatMessageDto.from(m, userId))
                .collect(Collectors.toList());
    }

    /**
     * 메시지 읽음 처리
     */
    @Transactional
    public void markAsRead(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findByIdWithUsers(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));

        assertParticipant(userId, roomId);

        // 마지막 메시지 ID 조회
        ChatMessage lastMessage = chatMessageRepository.findLastMessageByChatRoomId(roomId);
        if (lastMessage == null) {
            return; // 메시지가 없으면 처리할 것 없음
        }

        // ChatRead 업데이트 또는 생성
        ChatReadId readId = new ChatReadId(roomId, userId);
        ChatRead chatRead = chatReadRepository.findById(readId)
                .orElseGet(() -> ChatRead.create(room, userId, 0L));

        chatRead.updateLastReadMessage(lastMessage.getId());
        chatReadRepository.save(chatRead);

        // Notification 기반 미읽음 알림 읽음 처리
        notificationService.markChatRoomAsRead(userId, roomId);

        // 트랜잭션 커밋 후 읽음 이벤트 Redis 발행 (Transactional Outbox 패턴)
        Long lastReadId = lastMessage.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                doPublishReadEvent(roomId, userId, lastReadId);
            }
        });

        log.info("메시지 읽음 처리: roomId={}, userId={}, lastReadMessageId={}", roomId, userId, lastMessage.getId());
    }

    /**
     * 메시지 삭제 (본인 기준)
     */
    @Transactional
    public void deleteMessages(Long roomId, Long userId, List<Long> messageIds) {
        assertParticipant(userId, roomId);

        for (Long messageId : messageIds) {
            // 이미 삭제했는지 확인
            if (chatMessageDeleteRepository.existsById_ChatMessageIdAndId_UserId(messageId, userId)) {
                continue;
            }

            ChatMessage message = chatMessageRepository.findById(messageId)
                    .orElseThrow(() -> new ChatMessageNotFoundException(messageId));

            // 해당 채팅방의 메시지인지 확인
            if (!message.getChatRoom().getId().equals(roomId)) {
                throw new ChatInvalidMessageException(messageId);
            }

            ChatMessageDelete messageDelete = ChatMessageDelete.create(message, userId);
            chatMessageDeleteRepository.save(messageDelete);
        }

        log.info("메시지 삭제: roomId={}, userId={}, count={}", roomId, userId, messageIds.size());
    }

    // === Private Helper Methods ===

    /**
     * Redis Pub/Sub 메시지 발행 (afterCommit 내부 전용)
     * afterCommit에서 발생하는 예외는 Spring이 억제하므로 로그로 대체
     */
    private void doPublishToRedis(Long roomId, Long messageId, ChatPayloadResponseDto payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            String channel = CHAT_CHANNEL_PREFIX + roomId;
            redisTemplate.convertAndSend(channel, json);
            log.info("Redis 메시지 발행: channel={}, messageId={}", channel, messageId);
        } catch (JsonProcessingException e) {
            log.error("채팅 메시지 직렬화 실패: roomId={}, messageId={}", roomId, messageId, e);
        }
    }

    /**
     * 읽음 이벤트 Redis 발행 (afterCommit 내부 전용)
     * afterCommit에서 발생하는 예외는 Spring이 억제하므로 로그로 대체
     */
    private void doPublishReadEvent(Long roomId, Long userId, Long lastReadMessageId) {
        ChatPayloadResponseDto payload = ChatPayloadResponseDto.builder()
                .roomId(roomId)
                .readerId(userId)
                .readUpToMessageId(lastReadMessageId)
                .system(true)
                .build();

        try {
            String json = objectMapper.writeValueAsString(payload);
            String channel = CHAT_CHANNEL_PREFIX + roomId;
            redisTemplate.convertAndSend(channel, json);
            log.info("읽음 이벤트 발행: channel={}, readerId={}", channel, userId);
        } catch (JsonProcessingException e) {
            log.error("읽음 이벤트 직렬화 실패: roomId={}, userId={}", roomId, userId, e);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }
}