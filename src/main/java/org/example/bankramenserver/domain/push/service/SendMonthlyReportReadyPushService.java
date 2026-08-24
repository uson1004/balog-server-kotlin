package org.example.bankramenserver.domain.push.service;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.push.domain.PushNotification;
import org.example.bankramenserver.domain.push.domain.repository.PushNotificationRepository;
import org.example.bankramenserver.domain.transaction.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SendMonthlyReportReadyPushService {

    private static final String TITLE = "월간 리포트가 준비됐어요";

    private final TransactionRepository transactionRepository;
    private final PushNotificationRepository pushNotificationRepository;
    private final SendPushNotificationService sendPushNotificationService;

    @Transactional
    public void execute(YearMonth yearMonth) {
        transactionRepository.findUserIdsHavingTransactionsBetween(
                        yearMonth.atDay(1),
                        yearMonth.atEndOfMonth()
                )
                .stream()
                .filter(userId -> shouldSend(userId, yearMonth))
                .forEach(userId -> send(userId, yearMonth));
    }

    private boolean shouldSend(UUID userId, YearMonth yearMonth) {
        return !pushNotificationRepository.existsByUser_IdAndTypeAndReferenceKey(
                userId,
                PushNotification.NotificationType.MONTHLY_REPORT,
                yearMonth.toString()
        );
    }

    private void send(UUID userId, YearMonth yearMonth) {
        sendPushNotificationService.execute(
                userId,
                PushNotification.NotificationType.MONTHLY_REPORT,
                TITLE,
                "%s 소비 리포트를 확인해보세요.".formatted(yearMonth),
                yearMonth.toString(),
                Map.of(
                        "type", PushNotification.NotificationType.MONTHLY_REPORT.name(),
                        "yearMonth", yearMonth.toString()
                )
        );
    }
}
