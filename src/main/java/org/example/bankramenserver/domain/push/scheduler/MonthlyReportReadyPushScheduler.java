package org.example.bankramenserver.domain.push.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.push.service.SendMonthlyReportReadyPushService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class MonthlyReportReadyPushScheduler {

    private final SendMonthlyReportReadyPushService sendMonthlyReportReadyPushService;
    private final Clock clock;

    @Scheduled(cron = "0 0 9 1 * *", zone = "Asia/Seoul")
    public void sendMonthlyReportReadyPush() {
        YearMonth targetMonth = YearMonth.now(clock).minusMonths(1);
        sendMonthlyReportReadyPushService.execute(targetMonth);
    }
}
