package org.example.balogserver.domain.push.scheduler

import org.example.balogserver.domain.push.service.SendMonthlyReportReadyPushService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.time.Clock
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

class MonthlyReportReadyPushSchedulerTest {
    @Test fun sendMonthlyReportReadyPushTargetsPreviousMonth() {
        val service = mock(SendMonthlyReportReadyPushService::class.java)
        MonthlyReportReadyPushScheduler(service, Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneId.of("Asia/Seoul"))).sendMonthlyReportReadyPush()
        verify(service).execute(YearMonth.of(2026, 5))
    }
}
