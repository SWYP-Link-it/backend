package org.swyp.linkit.domain.search.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchKeywordRecorder {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "stat:search:keyword:";
    private static final long TTL_HOURS = 48;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 검색어를 Redis Hash에 누적
    public void record(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        try {
            String key = KEY_PREFIX + LocalDate.now(KST);
            String field = keyword.strip().toLowerCase(Locale.ROOT);
            boolean isNewKey = !Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
            stringRedisTemplate.opsForHash().increment(key, field, 1);
            // 키 신규 생성 시에만 TTL 설정
            if (isNewKey) {
                stringRedisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
            }
            log.debug("검색어 집계: keyword='{}', date={}", field, LocalDate.now(KST));
        } catch (Exception e) {
            log.warn("검색어 Redis 집계 실패 (유실 허용): keyword='{}', error={}", keyword, e.getMessage());
        }
    }
}
