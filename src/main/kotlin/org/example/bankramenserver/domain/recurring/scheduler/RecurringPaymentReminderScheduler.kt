package org.example.bankramenserver.domain.recurring.scheduler

import org.example.bankramenserver.domain.recurring.service.SendRecurringPaymentReminderService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RecurringPaymentReminderScheduler(private val sendRecurringPaymentReminderService: SendRecurringPaymentReminderService) {
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    fun sendReminder() = sendRecurringPaymentReminderService.execute()
}
