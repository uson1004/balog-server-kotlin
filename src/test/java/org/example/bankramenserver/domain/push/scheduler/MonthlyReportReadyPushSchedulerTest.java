package org.example.bankramenserver.domain.push.scheduler;

import org.example.bankramenserver.domain.push.service.SendMonthlyReportReadyPushService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MonthlyReportReadyPushSchedulerTest {

    @Test
    void sendMonthlyReportReadyPushTargetsPreviousMonth() {
        SendMonthlyReportReadyPushService service = mock(SendMonthlyReportReadyPushService.class);
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        MonthlyReportReadyPushScheduler scheduler = new MonthlyReportReadyPushScheduler(service, clock);

        scheduler.sendMonthlyReportReadyPush();

        verify(service).execute(YearMonth.of(2026, 5));
    }
}
