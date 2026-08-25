package org.example.bankramenserver.domain.push.service

import org.example.bankramenserver.domain.push.domain.PushNotification
import org.example.bankramenserver.domain.push.domain.repository.PushNotificationRepository
import org.example.bankramenserver.domain.transaction.domain.repository.TransactionRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class SendMonthlyReportReadyPushServiceTest {
    @Mock lateinit var transactionRepository: TransactionRepository
    @Mock lateinit var pushNotificationRepository: PushNotificationRepository
    @Mock lateinit var sendPushNotificationService: SendPushNotificationService
    @Test fun executeSendsMonthlyReportReadyPushToUsersWithTransactions() {
        val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        `when`(transactionRepository.findUserIdsHavingTransactionsBetween(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31))).thenReturn(listOf(userId))
        `when`(pushNotificationRepository.existsByUser_IdAndTypeAndReferenceKey(userId, PushNotification.NotificationType.MONTHLY_REPORT, "2026-05")).thenReturn(false)
        SendMonthlyReportReadyPushService(transactionRepository, pushNotificationRepository, sendPushNotificationService).execute(YearMonth.of(2026, 5))
        verify(sendPushNotificationService).execute(userId, PushNotification.NotificationType.MONTHLY_REPORT, "월간 리포트가 준비됐어요", "2026-05 소비 리포트를 확인해보세요.", "2026-05", mapOf("type" to PushNotification.NotificationType.MONTHLY_REPORT.name, "yearMonth" to "2026-05"))
    }
}
