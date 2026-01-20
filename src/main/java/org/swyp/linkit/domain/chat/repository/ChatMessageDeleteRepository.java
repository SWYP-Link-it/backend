package org.swyp.linkit.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.chat.entity.ChatMessageDelete;
import org.swyp.linkit.domain.chat.entity.ChatMessageDeleteId;

import java.util.List;

public interface ChatMessageDeleteRepository extends JpaRepository<ChatMessageDelete, ChatMessageDeleteId> {

    /**
     * 특정 사용자가 삭제한 메시지 ID 목록 조회
     */
    @Query("SELECT d.id.chatMessageId FROM ChatMessageDelete d WHERE d.id.userId = :userId")
    List<Long> findDeletedMessageIdsByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자가 해당 메시지를 삭제했는지 확인
     */
    boolean existsById_ChatMessageIdAndId_UserId(Long chatMessageId, Long userId);
}