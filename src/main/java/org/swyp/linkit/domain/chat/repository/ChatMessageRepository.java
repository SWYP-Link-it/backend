package org.swyp.linkit.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.chat.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 채팅방의 최근 메시지 조회 (최신순 50개)
     */
    @Query(value = "SELECT * FROM chat_message m WHERE m.chat_room_id = :roomId ORDER BY m.chat_message_id DESC LIMIT 50", nativeQuery = true)
    List<ChatMessage> findTop50ByChatRoomIdOrderByIdDesc(@Param("roomId") Long roomId);

    /**
     * 채팅방의 모든 메시지 조회 (생성순)
     */
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    /**
     * 특정 메시지 ID 이후의 메시지 조회
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :roomId AND m.id > :messageId ORDER BY m.id ASC")
    List<ChatMessage> findByChatRoomIdAndIdGreaterThan(@Param("roomId") Long roomId, @Param("messageId") Long messageId);

    /**
     * 채팅방의 마지막 메시지 조회
     */
    @Query(value = "SELECT * FROM chat_message m WHERE m.chat_room_id = :roomId ORDER BY m.chat_message_id DESC LIMIT 1", nativeQuery = true)
    ChatMessage findLastMessageByChatRoomId(@Param("roomId") Long roomId);

    /**
     * 특정 메시지 ID 이후의 메시지 개수 조회 (읽지 않은 메시지 수 계산용)
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.id = :roomId AND m.id > :messageId")
    long countByChatRoomIdAndIdGreaterThan(@Param("roomId") Long roomId, @Param("messageId") Long messageId);
}