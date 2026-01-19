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
     * 멘토와 멘티 간의 기존 채팅방 조회
     */
    @Query("SELECT r FROM ChatRoom r WHERE " +
           "(r.mentorId = :mentorId AND r.menteeId = :menteeId) OR " +
           "(r.mentorId = :menteeId AND r.menteeId = :mentorId)")
    Optional<ChatRoom> findByMentorIdAndMenteeId(@Param("mentorId") Long mentorId,
                                                  @Param("menteeId") Long menteeId);

    /**
     * 특정 사용자가 참여한 모든 채팅방 조회 (멘토 또는 멘티)
     */
    @Query("SELECT r FROM ChatRoom r WHERE r.mentorId = :userId OR r.menteeId = :userId ORDER BY COALESCE(r.lastMessageAt, r.createdAt) DESC")
    List<ChatRoom> findAllByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자가 멘토로 참여한 채팅방 조회
     */
    List<ChatRoom> findByMentorIdOrderByLastMessageAtDesc(Long mentorId);

    /**
     * 특정 사용자가 멘티로 참여한 채팅방 조회
     */
    List<ChatRoom> findByMenteeIdOrderByLastMessageAtDesc(Long menteeId);

    /**
     * 특정 상태의 채팅방 조회
     */
    List<ChatRoom> findByStatus(ChatRoomStatus status);

    /**
     * 사용자가 해당 채팅방의 참여자인지 확인
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ChatRoom r " +
           "WHERE r.id = :roomId AND (r.mentorId = :userId OR r.menteeId = :userId)")
    boolean existsByIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);
}