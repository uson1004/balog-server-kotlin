package org.example.balogserver.domain.push.service

import org.example.balogserver.domain.push.domain.PushNotification
import org.example.balogserver.domain.push.domain.repository.PushNotificationRepository
import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth
import java.util.UUID

@Service
class SendMonthlyReportReadyPushService(
    private val transactionRepository: TransactionRepository,
    private val pushNotificationRepository: PushNotificationRepository,
    private val sendPushNotificationService: SendPushNotificationService,
) {
    @Transactional
    fun execute(yearMonth: YearMonth) {
        transactionRepository.findUserIdsHavingTransactionsBetween(yearMonth.atDay(1), yearMonth.atEndOfMonth())
            .filter { shouldSend(it, yearMonth) }
            .forEach { send(it, yearMonth) }
    }

    private fun shouldSend(userId: UUID, yearMonth: YearMonth) = !pushNotificationRepository.existsByUser_IdAndTypeAndReferenceKey(userId, PushNotification.NotificationType.MONTHLY_REPORT, yearMonth.toString())
    private fun send(userId: UUID, yearMonth: YearMonth) = sendPushNotificationService.execute(
        userId, PushNotification.NotificationType.MONTHLY_REPORT, TITLE, "$yearMonth 소비 리포트를 확인해보세요.", yearMonth.toString(),
        mapOf("type" to PushNotification.NotificationType.MONTHLY_REPORT.name, "yearMonth" to yearMonth.toString()),
    )
    private companion object { const val TITLE = "월간 리포트가 준비됐어요" }
}
