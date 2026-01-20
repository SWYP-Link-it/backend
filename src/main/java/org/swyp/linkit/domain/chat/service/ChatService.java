package org.swyp.linkit.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.chat.dto.ChatMessageDto;
import org.swyp.linkit.domain.chat.dto.response.ChatPayloadResponseDto;
import org.swyp.linkit.domain.chat.entity.*;
import org.swyp.linkit.domain.chat.repository.ChatMessageDeleteRepository;
import org.swyp.linkit.domain.chat.repository.ChatMessageRepository;
import org.swyp.linkit.domain.chat.repository.ChatReadRepository;
import org.swyp.linkit.domain.chat.repository.ChatRoomRepository;
import org.swyp.linkit.global.error.exception.ChatInvalidMessageException;
import org.swyp.linkit.global.error.exception.ChatMessageNotFoundException;
import org.swyp.linkit.global.error.exception.ChatNotParticipantException;
import org.swyp.linkit.global.error.exception.ChatRoomNotFoundException;

import java.time.LocalDateTime;
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
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CHAT_CHANNEL_PREFIX = "chat:room:";

    /**
     * 사용자가 채팅방 참여자인지 확인
     */
    public void assertParticipant(Long userId, Long roomId) {
        // 디버깅: 채팅방 정보 조회
        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
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
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));

        // 발신자 역할 결정
        SenderRole senderRole = room.getMentorId().equals(senderId) ? SenderRole.MENTOR : SenderRole.MENTEE;

        ChatMessage message = ChatMessage.create(room, senderId, senderRole, content);

        ChatMessage saved = chatMessageRepository.save(message);

        // 채팅방의 마지막 메시지 정보 업데이트
        room.updateLastMessage(saved.getId(), saved.getCreatedAt());

        // 상대방의 읽지 않은 메시지 수 증가
        if (senderRole == SenderRole.MENTOR) {
            room.incrementUnreadMenteeCount();
        } else {
            room.incrementUnreadMentorCount();
        }

        log.info("메시지 저장: roomId={}, senderId={}, messageId={}", roomId, senderId, saved.getId());
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
        ChatRoom room = chatRoomRepository.findById(roomId)
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

        // 읽지 않은 메시지 수 초기화
        boolean isMentor = room.getMentorId().equals(userId);
        if (isMentor) {
            room.resetUnreadMentorCount();
        } else {
            room.resetUnreadMenteeCount();
        }

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

    /**
     * Redis Pub/Sub을 통해 메시지 발행
     */
    public void publishToRedis(ChatMessage message) {
        ChatPayloadResponseDto payload = ChatPayloadResponseDto.builder()
                .roomId(message.getChatRoom().getId())
                .messageId(message.getId())
                .senderId(message.getSenderId())
                .senderRole(message.getSenderRole().name())
                .text(message.getContent())
                .sentAtEpochMs(message.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .system(false)
                .build();

        try {
            String json = objectMapper.writeValueAsString(payload);
            String channel = CHAT_CHANNEL_PREFIX + message.getChatRoom().getId();
            redisTemplate.convertAndSend(channel, json);
            log.info("Redis 메시지 발행: channel={}, messageId={}", channel, message.getId());
        } catch (JsonProcessingException e) {
            log.error("ChatPayload 직렬화 실패", e);
        }
    }

    /**
     * 읽음 처리 이벤트 Redis 발행
     */
    public void publishReadEvent(Long roomId, Long userId, Long lastReadMessageId) {
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
            log.error("읽음 이벤트 직렬화 실패", e);
        }
    }
}