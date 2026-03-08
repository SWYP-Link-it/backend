package org.swyp.linkit.domain.search.service;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.search.repository.SearchKeywordStatRepository;
import org.swyp.linkit.domain.search.repository.SkillViewStatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatFlushService {

    private final StringRedisTemplate stringRedisTemplate;
    private final SkillViewStatRepository skillViewStatRepository;
    private final SearchKeywordStatRepository searchKeywordStatRepository;

    // Redis 키 삭제 후 DB 업데이트
    @Transactional
    public int flushSkillViewKey(String key) {
        try {
            // 날짜 파싱: stat:skill:view:{yyyy-MM-dd}
            String datePart = key.substring("stat:skill:view:".length());
            LocalDate statDate = LocalDate.parse(datePart);

            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
            if (entries.isEmpty()) {
                stringRedisTemplate.delete(key);
                return 0;
            }

            stringRedisTemplate.delete(key);
            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                Long skillId = Long.parseLong(entry.getKey().toString());
                long count = Long.parseLong(entry.getValue().toString());
                skillViewStatRepository.upsertAdd(statDate, skillId, count);
            }

            log.debug("스킬 조회수 flush 완료: key={}, fields={}", key, entries.size());
            return entries.size();
        } catch (Exception e) {
            log.warn("스킬 조회수 flush 실패 (유실 허용): key={}, error={}", key, e.getMessage());
            return 0;
        }
    }

    // Redis 키 삭제 후 DB 업데이트
    @Transactional
    public int flushSearchKeywordKey(String key) {
        try {
            // 날짜 파싱: stat:search:keyword:{yyyy-MM-dd}
            String datePart = key.substring("stat:search:keyword:".length());
            LocalDate statDate = LocalDate.parse(datePart);

            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
            if (entries.isEmpty()) {
                stringRedisTemplate.delete(key);
                return 0;
            }

            stringRedisTemplate.delete(key);
            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                String keyword = entry.getKey().toString();
                long count = Long.parseLong(entry.getValue().toString());
                searchKeywordStatRepository.upsertAdd(statDate, keyword, count);
            }

            log.debug("검색어 카운트 flush 완료: key={}, fields={}", key, entries.size());
            return entries.size();
        } catch (Exception e) {
            log.warn("검색어 카운트 flush 실패 (유실 허용): key={}, error={}", key, e.getMessage());
            return 0;
        }
    }
}
