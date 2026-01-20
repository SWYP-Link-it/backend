package org.swyp.linkit.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.chat.entity.ChatRoomDelete;
import org.swyp.linkit.domain.chat.entity.ChatRoomDeleteId;

import java.util.List;

public interface ChatRoomDeleteRepository extends JpaRepository<ChatRoomDelete, ChatRoomDeleteId> {

    /**
     * 특정 사용자가 삭제한 채팅방 ID 목록 조회
     */
    @Query("SELECT d.id.chatRoomId FROM ChatRoomDelete d WHERE d.id.userId = :userId")
    List<Long> findDeletedRoomIdsByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자가 해당 채팅방을 삭제했는지 확인
     */
    boolean existsById_ChatRoomIdAndId_UserId(Long chatRoomId, Long userId);
}