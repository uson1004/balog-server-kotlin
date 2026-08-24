package org.example.bankramenserver.domain.recurring.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.recurring.service.CheckRecurringPaymentBillingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecurringPaymentBillingCheckScheduler {

    private final CheckRecurringPaymentBillingService checkRecurringPaymentBillingService;

    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    public void checkBilling() {
        checkRecurringPaymentBillingService.execute();
    }
}