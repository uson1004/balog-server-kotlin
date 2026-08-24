package org.example.bankramenserver.domain.push.service;

import org.example.bankramenserver.domain.push.domain.PushNotification;
import org.example.bankramenserver.domain.push.domain.repository.PushNotificationRepository;
import org.example.bankramenserver.domain.transaction.domain.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendMonthlyReportReadyPushServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PushNotificationRepository pushNotificationRepository;

    @Mock
    private SendPushNotificationService sendPushNotificationService;

    @Test
    void executeSendsMonthlyReportReadyPushToUsersWithTransactions() {
        SendMonthlyReportReadyPushService service = new SendMonthlyReportReadyPushService(
                transactionRepository,
                pushNotificationRepository,
                sendPushNotificationService
        );
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        YearMonth yearMonth = YearMonth.of(2026, 5);
        when(transactionRepository.findUserIdsHavingTransactionsBetween(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31)
        )).thenReturn(List.of(userId));
        when(pushNotificationRepository.existsByUser_IdAndTypeAndReferenceKey(
                userId,
                PushNotification.NotificationType.MONTHLY_REPORT,
                "2026-05"
        )).thenReturn(false);

        service.execute(yearMonth);

        verify(sendPushNotificationService).execute(
                userId,
                PushNotification.NotificationType.MONTHLY_REPORT,
                "월간 리포트가 준비됐어요",
                "2026-05 소비 리포트를 확인해보세요.",
                "2026-05",
                Map.of(
                        "type", PushNotification.NotificationType.MONTHLY_REPORT.name(),
                        "yearMonth", "2026-05"
                )
        );
    }
}
