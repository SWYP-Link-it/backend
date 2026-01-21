package org.swyp.linkit.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.linkit.domain.chat.entity.ChatRead;
import org.swyp.linkit.domain.chat.entity.ChatReadId;

import java.util.Optional;

public interface ChatReadRepository extends JpaRepository<ChatRead, ChatReadId> {

    /**
     * 특정 사용자의 채팅방 읽음 정보 조회
     */
    Optional<ChatRead> findById_ChatRoomIdAndId_UserId(Long chatRoomId, Long userId);
}