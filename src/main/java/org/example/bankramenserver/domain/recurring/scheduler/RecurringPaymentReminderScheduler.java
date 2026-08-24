package org.example.bankramenserver.domain.recurring.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.recurring.service.SendRecurringPaymentReminderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecurringPaymentReminderScheduler {

    private final SendRecurringPaymentReminderService sendRecurringPaymentReminderService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendReminder() {
        sendRecurringPaymentReminderService.execute();
    }
}