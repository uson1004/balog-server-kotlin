package org.example.bankramenserver.domain.recurring.service

import org.example.bankramenserver.domain.push.domain.PushNotification
import org.example.bankramenserver.domain.push.service.SendPushNotificationService
import org.example.bankramenserver.domain.recurring.domain.RecurringPayment
import org.example.bankramenserver.domain.recurring.domain.RecurringPaymentTransaction
import org.example.bankramenserver.domain.recurring.domain.repository.RecurringPaymentRepository
import org.example.bankramenserver.domain.transaction.domain.Transaction
import org.example.bankramenserver.domain.transaction.domain.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class CheckRecurringPaymentBillingService(
    private val recurringPaymentRepository: RecurringPaymentRepository,
    private val transactionRepository: TransactionRepository,
    private val sendPushNotificationService: SendPushNotificationService,
    private val clock: Clock,
) {
    @Transactional
    fun execute() {
        val today = LocalDate.now(clock)
        recurringPaymentRepository.findAllByActiveTrueAndConfirmedTrueAndNextBillingDateGreaterThanEqualAndNextBillingDateLessThan(today.atStartOfDay(), today.plusDays(1).atStartOfDay()).forEach(::check)
    }

    private fun check(recurringPayment: RecurringPayment) {
        val expectedDate = recurringPayment.nextBillingDate.toLocalDate()
        val transactions = transactionRepository.findAllByUser_IdAndTypeAndDescriptionAndAmountAndTransactionDateBetweenOrderByTransactionDateDesc(
            recurringPayment.user.id, Transaction.TransactionType.EXPENSE, recurringPayment.name, recurringPayment.amount,
            expectedDate.minusDays(toleranceDays(recurringPayment.cycle).toLong()), expectedDate.plusDays(toleranceDays(recurringPayment.cycle).toLong()),
        )
        if (transactions.isEmpty()) return sendMissingNotification(recurringPayment)
        recurringPayment.addTransaction(transactions.first(), RecurringPaymentTransaction.MatchType.PAYMENT_CONFIRMED, LocalDateTime.now(clock))
        sendPaidNotification(recurringPayment)
        recurringPayment.updateAfterPaymentDetected(recurringPayment.calculateNextBillingDate())
    }

    private fun toleranceDays(cycle: RecurringPayment.Cycle) = if (cycle == RecurringPayment.Cycle.MONTHLY) 3 else 7
    private fun sendPaidNotification(recurringPayment: RecurringPayment) = sendPushNotificationService.execute(
        recurringPayment.user.id!!, PushNotification.NotificationType.RECURRING_PAYMENT_CONFIRMED, "정기결제가 확인됐어요",
        "${recurringPayment.name} ${recurringPayment.amount}원이 정상적으로 결제됐어요.", "${recurringPayment.id}:PAID:${recurringPayment.nextBillingDate.toLocalDate()}",
        mapOf("type" to PushNotification.NotificationType.RECURRING_PAYMENT_CONFIRMED.name, "recurringPaymentId" to recurringPayment.id.toString()),
    )
    private fun sendMissingNotification(recurringPayment: RecurringPayment) = sendPushNotificationService.execute(
        recurringPayment.user.id!!, PushNotification.NotificationType.RECURRING_PAYMENT_MISSING, "정기결제 확인이 필요해요",
        "${recurringPayment.name} ${recurringPayment.amount}원 결제가 아직 확인되지 않았어요.", "${recurringPayment.id}:MISSING:${recurringPayment.nextBillingDate.toLocalDate()}",
        mapOf("type" to PushNotification.NotificationType.RECURRING_PAYMENT_MISSING.name, "recurringPaymentId" to recurringPayment.id.toString()),
    )
}
