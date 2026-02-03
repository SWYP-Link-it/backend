package org.swyp.linkit.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.chat.entity.ChatRoom;
import org.swyp.linkit.domain.chat.entity.ChatRoomStatus;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 멘토와 멘티 간의 기존 채팅방 조회 (fetch join으로 User 함께 로딩)
     */
    @Query("SELECT r FROM ChatRoom r " +
           "JOIN FETCH r.mentor " +
           "JOIN FETCH r.mentee " +
           "WHERE (r.mentor.id = :mentorId AND r.mentee.id = :menteeId) " +
           "OR (r.mentor.id = :menteeId AND r.mentee.id = :mentorId)")
    Optional<ChatRoom> findByMentorIdAndMenteeId(@Param("mentorId") Long mentorId,
                                                  @Param("menteeId") Long menteeId);

    /**
     * 특정 사용자가 참여한 모든 채팅방 조회 (멘토 또는 멘티)
     */
    @Query("SELECT r FROM ChatRoom r " +
           "JOIN FETCH r.mentor " +
           "JOIN FETCH r.mentee " +
           "WHERE r.mentor.id = :userId OR r.mentee.id = :userId " +
           "ORDER BY COALESCE(r.lastMessageAt, r.createdAt) DESC")
    List<ChatRoom> findAllByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자가 참여한 모든 채팅방 조회 (멘토, 멘티, 마지막 메시지 함께 fetch join)
     * 결과: [ChatRoom, ChatMessage(lastMessage)]
     */
    @Query("SELECT r, msg " +
           "FROM ChatRoom r " +
           "JOIN FETCH r.mentor " +
           "JOIN FETCH r.mentee " +
           "LEFT JOIN ChatMessage msg ON r.lastMessageId = msg.id " +
           "WHERE r.mentor.id = :userId OR r.mentee.id = :userId " +
           "ORDER BY COALESCE(r.lastMessageAt, r.createdAt) DESC")
    List<Object[]> findAllByUserIdWithDetails(@Param("userId") Long userId);

    /**
     * 특정 사용자가 멘토로 참여한 채팅방 조회
     */
    @Query("SELECT r FROM ChatRoom r " +
           "JOIN FETCH r.mentor " +
           "JOIN FETCH r.mentee " +
           "WHERE r.mentor.id = :mentorId " +
           "ORDER BY r.lastMessageAt DESC")
    List<ChatRoom> findByMentorIdOrderByLastMessageAtDesc(@Param("mentorId") Long mentorId);

    /**
     * 특정 사용자가 멘티로 참여한 채팅방 조회
     */
    @Query("SELECT r FROM ChatRoom r " +
           "JOIN FETCH r.mentor " +
           "JOIN FETCH r.mentee " +
           "WHERE r.mentee.id = :menteeId " +
           "ORDER BY r.lastMessageAt DESC")
    List<ChatRoom> findByMenteeIdOrderByLastMessageAtDesc(@Param("menteeId") Long menteeId);

    /**
     * 특정 상태의 채팅방 조회
     */
    @Query("SELECT r FROM ChatRoom r " +
           "JOIN FETCH r.mentor " +
           "JOIN FETCH r.mentee " +
           "WHERE r.status = :status")
    List<ChatRoom> findByStatus(@Param("status") ChatRoomStatus status);

    /**
     * 사용자가 해당 채팅방의 참여자인지 확인
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ChatRoom r " +
           "WHERE r.id = :roomId AND (r.mentor.id = :userId OR r.mentee.id = :userId)")
    boolean existsByIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 채팅방 단건 조회 (fetch join)
     */
    @Query("SELECT r FROM ChatRoom r " +
           "JOIN FETCH r.mentor " +
           "JOIN FETCH r.mentee " +
           "WHERE r.id = :roomId")
    Optional<ChatRoom> findByIdWithUsers(@Param("roomId") Long roomId);
}