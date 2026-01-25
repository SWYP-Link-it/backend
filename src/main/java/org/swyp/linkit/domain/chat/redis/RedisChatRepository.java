package org.swyp.linkit.domain.chat.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * Redis를 이용한 채팅 세션/입장 정보 관리
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisChatRepository {

    private final StringRedisTemplate redisTemplate;

    // Redis Key Prefix
    private static final String SESSION_USER_PREFIX = "ws:session:";      // 세션ID -> 유저ID
    private static final String USER_ROOM_PREFIX = "ws:user:room:";       // 유저ID -> 현재 입장한 채팅방ID
    private static final String ROOM_USERS_PREFIX = "ws:room:users:";     // 채팅방ID -> 입장한 유저 Set

    private static final long SESSION_TTL_HOURS = 24;

    // ==================== 세션-유저 매핑 ====================

    /**
     * WebSocket 세션과 유저 ID 매핑 저장
     */
    public void saveSessionUser(String sessionId, Long userId) {
        String key = SESSION_USER_PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, userId.toString(), SESSION_TTL_HOURS, TimeUnit.HOURS);
        log.debug("세션-유저 매핑 저장: sessionId={}, userId={}", sessionId, userId);
    }

    /**
     * 세션에 매핑된 유저 ID 조회
     */
    public Long getUserIdBySession(String sessionId) {
        String key = SESSION_USER_PREFIX + sessionId;
        String userId = redisTemplate.opsForValue().get(key);
        return userId != null ? Long.parseLong(userId) : null;
    }

    /**
     * 세션-유저 매핑 존재 여부 확인
     */
    public boolean existsSessionUser(String sessionId) {
        String key = SESSION_USER_PREFIX + sessionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 세션-유저 매핑 삭제
     */
    public void deleteSessionUser(String sessionId) {
        String key = SESSION_USER_PREFIX + sessionId;
        redisTemplate.delete(key);
        log.debug("세션-유저 매핑 삭제: sessionId={}", sessionId);
    }

    // ==================== 유저-채팅방 입장 정보 ====================

    /**
     * 유저의 채팅방 입장 정보 저장
     */
    public void enterChatRoom(Long userId, Long roomId) {
        String userRoomKey = USER_ROOM_PREFIX + userId;
        String roomUsersKey = ROOM_USERS_PREFIX + roomId;

        // 이전에 다른 채팅방에 있었다면 퇴장 처리
        String previousRoomId = redisTemplate.opsForValue().get(userRoomKey);
        if (previousRoomId != null && !previousRoomId.equals(roomId.toString())) {
            exitChatRoom(userId);
        }

        // 유저 -> 채팅방 매핑
        redisTemplate.opsForValue().set(userRoomKey, roomId.toString());

        // 채팅방 -> 유저 Set에 추가
        redisTemplate.opsForSet().add(roomUsersKey, userId.toString());

        log.info("채팅방 입장: userId={}, roomId={}", userId, roomId);
    }

    /**
     * 유저의 현재 입장한 채팅방 ID 조회
     */
    public Long getCurrentRoomId(Long userId) {
        String key = USER_ROOM_PREFIX + userId;
        String roomId = redisTemplate.opsForValue().get(key);
        return roomId != null ? Long.parseLong(roomId) : null;
    }

    /**
     * 유저가 채팅방에 입장해 있는지 확인
     */
    public boolean isUserInRoom(Long userId) {
        String key = USER_ROOM_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 유저의 채팅방 퇴장 처리
     */
    public Long exitChatRoom(Long userId) {
        String userRoomKey = USER_ROOM_PREFIX + userId;
        String roomIdStr = redisTemplate.opsForValue().get(userRoomKey);

        if (roomIdStr != null) {
            Long roomId = Long.parseLong(roomIdStr);
            String roomUsersKey = ROOM_USERS_PREFIX + roomId;

            // 채팅방 유저 Set에서 제거
            redisTemplate.opsForSet().remove(roomUsersKey, userId.toString());

            // 유저 -> 채팅방 매핑 삭제
            redisTemplate.delete(userRoomKey);

            log.info("채팅방 퇴장: userId={}, roomId={}", userId, roomId);
            return roomId;
        }
        return null;
    }

    // ==================== 채팅방 유저 관리 ====================

    /**
     * 채팅방에 입장한 유저 수 조회
     */
    public Long getRoomUserCount(Long roomId) {
        String key = ROOM_USERS_PREFIX + roomId;
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 특정 유저가 특정 채팅방에 있는지 확인
     */
    public boolean isUserInSpecificRoom(Long userId, Long roomId) {
        String key = ROOM_USERS_PREFIX + roomId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, userId.toString()));
    }

    /**
     * 유저의 전체 연결 정보 정리 (DISCONNECT 시 호출)
     */
    public void cleanupUserConnection(String sessionId) {
        Long userId = getUserIdBySession(sessionId);
        if (userId != null) {
            // 채팅방에서 퇴장
            exitChatRoom(userId);
            // 세션 정보 삭제
            deleteSessionUser(sessionId);
            log.info("유저 연결 정리 완료: sessionId={}, userId={}", sessionId, userId);
        }
    }
}
