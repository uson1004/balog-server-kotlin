package org.example.balogserver.domain.recurring.scheduler

import org.example.balogserver.domain.recurring.service.CheckRecurringPaymentBillingService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RecurringPaymentBillingCheckScheduler(private val checkRecurringPaymentBillingService: CheckRecurringPaymentBillingService) {
    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    fun checkBilling() = checkRecurringPaymentBillingService.execute()
}
