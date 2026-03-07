package org.swyp.linkit.domain.search.scheduler;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.swyp.linkit.domain.search.repository.SkillViewStatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatFlushScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final SkillViewStatRepository skillViewStatRepository;

    private static final String SKILL_VIEW_KEY_PATTERN = "stat:skill:view:*";

    // 스킬 조회수 Redis → DB flush
    @Scheduled(fixedDelayString = "${schedules.stat-flush-delay}")
    public void flushSkillViewStats() {
        log.info("== 스킬 조회수 flush 시작 ==");
        int totalFlushed = 0;

        try (var cursor = stringRedisTemplate.scan(
                ScanOptions.scanOptions().match(SKILL_VIEW_KEY_PATTERN).count(100).build())) {

            while (cursor.hasNext()) {
                totalFlushed += flushSkillViewKey(cursor.next());
            }
        } catch (Exception e) {
            log.warn("스킬 조회수 flush 중 오류 발생: error={}", e.getMessage());
        }

        log.info("== 스킬 조회수 flush 완료: totalFlushed={} ==", totalFlushed);
    }

    private int flushSkillViewKey(String key) {
        try {
            // 날짜 파싱: stat:skill:view:{yyyy-MM-dd}
            String datePart = key.substring("stat:skill:view:".length());
            LocalDate statDate = LocalDate.parse(datePart);

            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
            if (entries.isEmpty()) {
                stringRedisTemplate.delete(key);
                return 0;
            }

            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                Long skillId = Long.parseLong(entry.getKey().toString());
                long count = Long.parseLong(entry.getValue().toString());
                skillViewStatRepository.upsertAdd(statDate, skillId, count);
            }

            stringRedisTemplate.delete(key);
            log.debug("스킬 조회수 flush 완료: key={}, fields={}", key, entries.size());
            return entries.size();
        } catch (Exception e) {
            log.warn("스킬 조회수 flush 실패 (유실 허용): key={}, error={}", key, e.getMessage());
            return 0;
        }
    }
}
