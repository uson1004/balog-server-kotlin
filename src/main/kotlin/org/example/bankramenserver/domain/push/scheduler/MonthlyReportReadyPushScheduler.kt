package org.example.bankramenserver.domain.push.scheduler

import org.example.bankramenserver.domain.push.service.SendMonthlyReportReadyPushService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.YearMonth

@Component
class MonthlyReportReadyPushScheduler(
    private val sendMonthlyReportReadyPushService: SendMonthlyReportReadyPushService,
    private val clock: Clock,
) {
    @Scheduled(cron = "0 0 9 1 * *", zone = "Asia/Seoul")
    fun sendMonthlyReportReadyPush() = sendMonthlyReportReadyPushService.execute(YearMonth.now(clock).minusMonths(1))
}
