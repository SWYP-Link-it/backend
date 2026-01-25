package org.swyp.linkit.domain.auth.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PendingUserStorage {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "pending_user:";
    private static final long EXPIRATION_MINUTES = 10;

    // 임시 사용자 정보를 Redis에 저장
    public String savePendingUser(String pendingUserInfoJson) {
        String sessionId = UUID.randomUUID().toString();
        String key = KEY_PREFIX + sessionId;

        stringRedisTemplate.opsForValue().set(
                key,
                pendingUserInfoJson,
                EXPIRATION_MINUTES,
                TimeUnit.MINUTES
        );

        log.info("임시 사용자 정보 저장 완료 - sessionId: {}", sessionId);

        return sessionId;
    }

    // 세션 ID로 임시 사용자 정보 조회
    public Optional<String> getPendingUser(String sessionId) {
        String key = KEY_PREFIX + sessionId;
        String json = stringRedisTemplate.opsForValue().get(key);

        if (json == null) {
            log.warn("임시 사용자 정보 조회 실패 - sessionId: {} (만료되었거나 존재하지 않음)", sessionId);
        } else {
            log.info("임시 사용자 정보 조회 성공 - sessionId: {}", sessionId);
        }

        return Optional.ofNullable(json);
    }

    // 세션 ID로 임시 사용자 정보 삭제
    public void deletePendingUser(String sessionId) {
        String key = KEY_PREFIX + sessionId;
        Boolean deleted = stringRedisTemplate.delete(key);

        if (deleted) {
            log.info("임시 사용자 정보 삭제 완료 - sessionId: {}", sessionId);
        } else {
            log.warn("임시 사용자 정보 삭제 실패 - sessionId: {} (이미 삭제되었거나 존재하지 않음)", sessionId);
        }
    }
}