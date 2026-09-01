package org.example.balogserver.domain.recurring.service

import org.example.balogserver.domain.push.domain.PushNotification
import org.example.balogserver.domain.push.service.SendPushNotificationService
import org.example.balogserver.domain.recurring.domain.RecurringPayment
import org.example.balogserver.domain.recurring.domain.repository.RecurringPaymentRepository
import org.example.balogserver.domain.transaction.domain.Transaction
import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.example.balogserver.domain.user.domain.repository.UserRepository
import org.example.balogserver.global.util.MoneyFormatter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class DetectRecurringPaymentService(
    private val transactionRepository: TransactionRepository,
    private val recurringPaymentRepository: RecurringPaymentRepository,
    private val userRepository: UserRepository,
    private val sendPushNotificationService: SendPushNotificationService,
) {
    @Transactional
    fun execute(transactionId: UUID) {
        val current = transactionRepository.findById(transactionId).orElseThrow()
        if (current.type != Transaction.TransactionType.EXPENSE || current.description.isNullOrBlank()) return
        detect(current, RecurringPayment.Cycle.MONTHLY, current.transactionDate.minusMonths(1), 3)
        detect(current, RecurringPayment.Cycle.YEARLY, current.transactionDate.minusYears(1), 7)
    }

    private fun detect(current: Transaction, cycle: RecurringPayment.Cycle, expectedDate: LocalDate, toleranceDays: Long) {
        if (transactionRepository.existsSameExpenseTransactionBetween(current.user.id, current.description, current.amount, expectedDate.minusDays(toleranceDays), expectedDate.plusDays(toleranceDays))) createCandidateIfNotExists(current, cycle)
    }

    private fun createCandidateIfNotExists(current: Transaction, cycle: RecurringPayment.Cycle) {
        val userId = current.user.id!!
        if (recurringPaymentRepository.existsByUser_IdAndNameAndAmountAndCycleAndActiveTrue(userId, current.description, current.amount, cycle)) return
        val recurringPayment = RecurringPayment.builder().user(userRepository.getReferenceById(userId)).category(current.category).name(current.description).amount(current.amount)
            .cycle(cycle).billingDay(current.transactionDate.dayOfMonth).nextBillingDate(nextBillingDate(current.transactionDate, cycle))
            .registrationType(RecurringPayment.RegistrationType.AUTO_DETECTED).confirmed(false).build()
        recurringPaymentRepository.save(recurringPayment)
        sendPushNotificationService.execute(
            userId, PushNotification.NotificationType.RECURRING_CANDIDATE, "정기결제 같아요",
            "${current.description} ${MoneyFormatter.format(current.amount)}원이 반복 결제되고 있어요. 정기결제로 등록할까요?", recurringPayment.id.toString(),
            mapOf("type" to PushNotification.NotificationType.RECURRING_CANDIDATE.name, "recurringPaymentId" to recurringPayment.id.toString()),
        )
    }

    private fun nextBillingDate(transactionDate: LocalDate, cycle: RecurringPayment.Cycle): LocalDateTime =
        (if (cycle == RecurringPayment.Cycle.MONTHLY) transactionDate.plusMonths(1) else transactionDate.plusYears(1)).atStartOfDay()
}
