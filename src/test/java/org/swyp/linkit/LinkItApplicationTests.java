package org.swyp.linkit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(TestRedisConfig.class)
@ActiveProfiles("test")
@SpringBootTest
class LinkItApplicationTests {

    @Test
    void contextLoads() {
    }
}
