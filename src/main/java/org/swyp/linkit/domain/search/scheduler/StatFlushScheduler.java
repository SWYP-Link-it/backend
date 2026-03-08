package org.swyp.linkit.domain.search.scheduler;

import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.swyp.linkit.domain.search.service.StatFlushService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatFlushScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final StatFlushService statFlushService;

    private static final String SKILL_VIEW_KEY_PATTERN = "stat:skill:view:*";
    private static final String SEARCH_KEYWORD_KEY_PATTERN = "stat:search:keyword:*";

    // 스킬 조회수 Redis → DB flush
    @Scheduled(fixedDelayString = "${schedules.stat-flush-delay}")
    public void flushSkillViewStats() {
        log.info("== 스킬 조회수 flush 시작 ==");
        int totalFlushed = 0;

        try (var cursor = stringRedisTemplate.scan(
                ScanOptions.scanOptions().match(SKILL_VIEW_KEY_PATTERN).count(100).build())) {

            while (cursor.hasNext()) {
                totalFlushed += statFlushService.flushSkillViewKey(cursor.next());
            }
        } catch (Exception e) {
            log.warn("스킬 조회수 flush 중 오류 발생: error={}", e.getMessage());
        }

        log.info("== 스킬 조회수 flush 완료: totalFlushed={} ==", totalFlushed);
    }

    // 검색어 카운트 Redis → DB flush
    @Scheduled(fixedDelayString = "${schedules.stat-flush-delay}")
    public void flushSearchKeywordStats() {
        log.info("== 검색어 카운트 flush 시작 ==");
        int totalFlushed = 0;

        try (var cursor = stringRedisTemplate.scan(
                ScanOptions.scanOptions().match(SEARCH_KEYWORD_KEY_PATTERN).count(100).build())) {

            while (cursor.hasNext()) {
                totalFlushed += statFlushService.flushSearchKeywordKey(cursor.next());
            }
        } catch (Exception e) {
            log.warn("검색어 카운트 flush 중 오류 발생: error={}", e.getMessage());
        }

        log.info("== 검색어 카운트 flush 완료: totalFlushed={} ==", totalFlushed);
    }
}
