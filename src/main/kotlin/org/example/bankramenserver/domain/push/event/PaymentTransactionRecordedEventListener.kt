package org.example.bankramenserver.domain.push.event

import org.example.bankramenserver.domain.push.domain.PushNotification
import org.example.bankramenserver.domain.push.service.SendPushNotificationService
import org.example.bankramenserver.domain.transaction.event.PaymentTransactionRecordedEvent
import org.example.bankramenserver.infrastructure.fcm.FcmAsyncConfig
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.text.NumberFormat
import java.util.Locale

@Component
class PaymentTransactionRecordedEventListener(
    private val sendPushNotificationService: SendPushNotificationService,
) {
    @Async(FcmAsyncConfig.FCM_PUSH_TASK_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: PaymentTransactionRecordedEvent) {
        sendPushNotificationService.execute(
            event.userId(),
            PushNotification.NotificationType.PAYMENT_RECORDED,
            TITLE,
            buildBody(event),
            event.transactionId().toString(),
            mapOf(
                "type" to PushNotification.NotificationType.PAYMENT_RECORDED.name,
                "transactionId" to event.transactionId().toString(),
            ),
        )
    }

    private fun buildBody(event: PaymentTransactionRecordedEvent): String =
        "${event.title()} ${NumberFormat.getNumberInstance(Locale.KOREA).format(event.amount())}원이 ${event.category()!!.displayName}으로 기록됐어요."

    private companion object { const val TITLE = "결제 내역이 기록됐어요" }
}
