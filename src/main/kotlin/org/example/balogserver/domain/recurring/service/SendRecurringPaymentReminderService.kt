package org.example.balogserver.domain.recurring.service

import org.example.balogserver.domain.push.domain.PushNotification
import org.example.balogserver.domain.push.service.SendPushNotificationService
import org.example.balogserver.domain.recurring.domain.RecurringPayment
import org.example.balogserver.domain.recurring.domain.repository.RecurringPaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.NumberFormat
import java.time.Clock
import java.time.LocalDate
import java.util.Locale

@Service
class SendRecurringPaymentReminderService(
    private val recurringPaymentRepository: RecurringPaymentRepository,
    private val sendPushNotificationService: SendPushNotificationService,
    private val clock: Clock,
) {
    @Transactional
    fun execute() {
        val tomorrow = LocalDate.now(clock).plusDays(1)
        recurringPaymentRepository.findAllByActiveTrueAndConfirmedTrueAndNextBillingDateBetween(tomorrow.atStartOfDay(), tomorrow.plusDays(1).atStartOfDay()).forEach(::sendReminder)
    }

    private fun sendReminder(recurringPayment: RecurringPayment) = sendPushNotificationService.execute(
        recurringPayment.user.id!!, PushNotification.NotificationType.RECURRING_PAYMENT_REMINDER, "내일 정기결제 예정이에요",
        "${recurringPayment.name} ${NumberFormat.getNumberInstance(Locale.KOREA).format(recurringPayment.amount)}원이 내일 결제될 예정이에요.",
        "${recurringPayment.id}:REMINDER:${recurringPayment.nextBillingDate.toLocalDate()}",
        mapOf("type" to PushNotification.NotificationType.RECURRING_PAYMENT_REMINDER.name, "recurringPaymentId" to recurringPayment.id.toString(), "billingDate" to recurringPayment.nextBillingDate.toLocalDate().toString()),
    )
}
