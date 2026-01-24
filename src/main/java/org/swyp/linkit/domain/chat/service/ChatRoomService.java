package org.swyp.linkit.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.chat.dto.ChatRoomDto;
import org.swyp.linkit.domain.chat.entity.ChatMessage;
import org.swyp.linkit.domain.chat.entity.ChatRoom;
import org.swyp.linkit.domain.chat.entity.ChatRoomDelete;
import org.swyp.linkit.domain.chat.entity.ChatRoomStatus;
import org.swyp.linkit.domain.chat.repository.ChatMessageRepository;
import org.swyp.linkit.domain.chat.repository.ChatRoomDeleteRepository;
import org.swyp.linkit.domain.chat.repository.ChatRoomRepository;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.error.exception.ChatInvalidUserException;
import org.swyp.linkit.global.error.exception.ChatNotParticipantException;
import org.swyp.linkit.global.error.exception.ChatRoomNotFoundException;
import org.swyp.linkit.global.error.exception.ChatSameUserException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomDeleteRepository chatRoomDeleteRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    /**
     * 1:1 채팅방 생성 또는 조회 (멘토-멘티)
     */
    @Transactional
    public ChatRoomDto createOrGetRoom(Long mentorId, Long menteeId) {
        validateUsers(mentorId, menteeId);

        // 기존 채팅방 확인
        return chatRoomRepository.findByMentorIdAndMenteeId(mentorId, menteeId)
                .map(room -> {
                    log.info("기존 채팅방 조회: roomId={}, mentorId={}, menteeId={}", room.getId(), mentorId, menteeId);
                    return ChatRoomDto.from(room);
                })
                .orElseGet(() -> {
                    // 새 채팅방 생성
                    ChatRoom newRoom = ChatRoom.create(mentorId, menteeId);
                    chatRoomRepository.save(newRoom);
                    log.info("새로운 채팅방 생성: roomId={}, mentorId={}, menteeId={}", newRoom.getId(), mentorId, menteeId);
                    return ChatRoomDto.from(newRoom);
                });
    }

    /**
     * 사용자가 참여한 모든 채팅방 조회 (삭제된 채팅방 제외, 마지막 메시지 내용 포함)
     */
    public List<ChatRoomDto> findRoomsByUserId(Long userId) {
        validateUserExists(userId);

        Set<Long> deletedRoomIds = Set.copyOf(chatRoomDeleteRepository.findDeletedRoomIdsByUserId(userId));

        // JPQL JOIN으로 한 번에 조회 (N+1 해결)
        // 결과: [ChatRoom, User(mentor), User(mentee), ChatMessage(lastMessage)]
        List<Object[]> results = chatRoomRepository.findAllByUserIdWithDetails(userId);

        return results.stream()
                .filter(row -> {
                    ChatRoom room = (ChatRoom) row[0];
                    return !deletedRoomIds.contains(room.getId());
                })
                .map(row -> {
                    ChatRoom room = (ChatRoom) row[0];
                    User mentor = (User) row[1];
                    User mentee = (User) row[2];
                    ChatMessage lastMessage = (ChatMessage) row[3];

                    // 마지막 메시지 내용
                    String lastMessageContent = lastMessage != null ? lastMessage.getContent() : null;

                    // 상대방 정보
                    boolean isMentor = room.getMentorId().equals(userId);
                    User partner = isMentor ? mentee : mentor;
                    String partnerNickname = partner != null ? partner.getNickname() : "알 수 없음";
                    String partnerProfileImageUrl = partner != null ? partner.getProfileImageUrl() : null;

                    return ChatRoomDto.fromWithPartner(room, userId, partnerNickname, partnerProfileImageUrl, lastMessageContent);
                })
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 ID로 조회
     */
    public ChatRoom findById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));
    }

    /**
     * 채팅방 ID로 DTO 조회
     */
    public ChatRoomDto findDtoById(Long roomId) {
        ChatRoom room = findById(roomId);
        return ChatRoomDto.from(room);
    }

    /**
     * 채팅방 상태 변경
     */
    @Transactional
    public void updateStatus(Long roomId, ChatRoomStatus status) {
        ChatRoom room = findById(roomId);
        room.changeStatus(status);
        log.info("채팅방 상태 변경: roomId={}, status={}", roomId, status);
    }

    /**
     * 채팅방 삭제 (본인 기준)
     */
    @Transactional
    public void deleteRooms(Long userId, List<Long> roomIds) {
        validateUserExists(userId);

        for (Long roomId : roomIds) {
            // 참여자 확인
            if (!isParticipant(roomId, userId)) {
                throw new ChatNotParticipantException(roomId, userId);
            }

            // 이미 삭제했는지 확인
            if (chatRoomDeleteRepository.existsById_ChatRoomIdAndId_UserId(roomId, userId)) {
                continue;
            }

            ChatRoom room = findById(roomId);
            ChatRoomDelete roomDelete = ChatRoomDelete.create(room, userId);
            chatRoomDeleteRepository.save(roomDelete);
        }

        log.info("채팅방 삭제: userId={}, count={}", userId, roomIds.size());
    }

    /**
     * 사용자가 해당 채팅방의 참여자인지 확인
     */
    public boolean isParticipant(Long roomId, Long userId) {
        return chatRoomRepository.existsByIdAndUserId(roomId, userId);
    }

    /**
     * 사용자가 해당 채팅방에서 멘토인지 확인
     */
    public boolean isMentor(ChatRoom room, Long userId) {
        return room.getMentorId().equals(userId);
    }

    // === Private Helper Methods ===

    private void validateUserExists(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ChatInvalidUserException();
        }
    }

    private void validateUsers(Long mentorId, Long menteeId) {
        if (mentorId == null || menteeId == null) {
            throw new ChatInvalidUserException();
        }
        if (mentorId.equals(menteeId)) {
            throw new ChatSameUserException();
        }
        validateUserExists(mentorId);
        validateUserExists(menteeId);
    }
}