package org.swyp.linkit.domain.exchange.scheduler;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.swyp.linkit.TestRedisConfig;
import org.swyp.linkit.domain.exchange.service.SkillExchangeService;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestRedisConfig.class)
@ActiveProfiles("test")
class SkillExchangeExpireSchedulerTest {

    @MockitoBean
    private SkillExchangeService exchangeService;

    @MockitoSpyBean
    private SkillExchangeExpireScheduler expireScheduler;

    @Test
    @DisplayName("크론 표헌식에 따라 스케줄러 메서드가 호출되어야한다.")
    public void SkillExchangeExpireSchedulerTest(){
        // given
        when(exchangeService.expirePendingRequests()).thenReturn(5);

        // when && then
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(expireScheduler, atLeast(2)).runExpireRequests();
                    verify(exchangeService, atLeast(2)).expirePendingRequests();
                });
    }
}