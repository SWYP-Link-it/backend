package org.swyp.linkit.domain.search.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
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

    private static final String SKILL_VIEW_KEY_PREFIX = "stat:skill:view:";
    private static final String SEARCH_KEYWORD_KEY_PREFIX = "stat:search:keyword:";

    private final StringRedisTemplate stringRedisTemplate;
    private final SkillViewStatRepository skillViewStatRepository;
    private final SearchKeywordStatRepository searchKeywordStatRepository;

    // HGETALL + DEL 원자적 수행
    private static final RedisScript<List> DRAIN_HASH_SCRIPT = RedisScript.of(
        "local data = redis.call('HGETALL', KEYS[1])\n" +
        "if #data > 0 then redis.call('DEL', KEYS[1]) end\n" +
        "return data",
        List.class
    );

    @Transactional
    public int flushSkillViewKey(String key) {
        // 날짜 파싱: stat:skill:view:{yyyy-MM-dd}
        String datePart = key.substring(SKILL_VIEW_KEY_PREFIX.length());
        LocalDate statDate = LocalDate.parse(datePart);

        List<String> data = stringRedisTemplate.execute(DRAIN_HASH_SCRIPT, List.of(key));
        if (data == null || data.isEmpty()) {
            return 0;
        }

        // HGETALL 결과는 [field1, value1, field2, value2, ...] 형태
        for (int i = 0; i < data.size(); i += 2) {
            Long skillId = Long.parseLong(data.get(i));
            long count = Long.parseLong(data.get(i + 1));
            skillViewStatRepository.upsertAdd(statDate, skillId, count);
        }

        log.debug("스킬 조회수 flush 완료: key={}, fields={}", key, data.size() / 2);
        return data.size() / 2;
    }

    @Transactional
    public int flushSearchKeywordKey(String key) {
        // 날짜 파싱: stat:search:keyword:{yyyy-MM-dd}
        String datePart = key.substring(SEARCH_KEYWORD_KEY_PREFIX.length());
        LocalDate statDate = LocalDate.parse(datePart);

        List<String> data = stringRedisTemplate.execute(DRAIN_HASH_SCRIPT, List.of(key));
        if (data == null || data.isEmpty()) {
            return 0;
        }

        // HGETALL 결과는 [field1, value1, field2, value2, ...] 형태
        for (int i = 0; i < data.size(); i += 2) {
            String keyword = data.get(i);
            long count = Long.parseLong(data.get(i + 1));
            searchKeywordStatRepository.upsertAdd(statDate, keyword, count);
        }

        log.debug("검색어 카운트 flush 완료: key={}, fields={}", key, data.size() / 2);
        return data.size() / 2;
    }
}
