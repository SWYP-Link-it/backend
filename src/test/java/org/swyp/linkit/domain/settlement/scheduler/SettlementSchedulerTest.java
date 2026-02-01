package org.swyp.linkit.domain.settlement.scheduler;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.swyp.linkit.TestRedisConfig;
import org.swyp.linkit.domain.settlement.service.SettlementService;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestRedisConfig.class)
@ActiveProfiles("test")
class SettlementSchedulerTest {

    @MockitoBean
    private SettlementService settlementService;

    @MockitoSpyBean
    private SettlementScheduler settlementScheduler;

    @Test
    @DisplayName("크론 표헌식에 따라 스케줄러 메서드가 호출되어야한다.")
    public void SettlementSchedulerTest(){
        // given
        when(settlementService.processAutoSettlement()).thenReturn(5);

        // when && then
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(settlementScheduler, atLeast(2)).runAutoRequests();
                    verify(settlementService, atLeast(2)).processAutoSettlement();
                });
    }

}