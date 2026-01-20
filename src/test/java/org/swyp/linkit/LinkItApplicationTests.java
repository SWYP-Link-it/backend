package org.swyp.linkit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class LinkItApplicationTests {

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @MockitoBean
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Test
    void contextLoads() {
    }
}
