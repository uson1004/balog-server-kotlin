package org.example.balogserver.infrastructure.integration

import org.example.balogserver.domain.transaction.event.PaymentTransactionRecordedEvent
import org.example.balogserver.infrastructure.fcm.FcmAsyncConfig
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class IntegrationOutboxEventListener(
    private val outboxWriter: IntegrationOutboxWriter,
    private val outboxDispatcher: IntegrationOutboxDispatcher,
) {
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun record(event: PaymentTransactionRecordedEvent) = outboxWriter.record(event)

    @Async(FcmAsyncConfig.FCM_PUSH_TASK_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun dispatch(@Suppress("UNUSED_PARAMETER") event: PaymentTransactionRecordedEvent) = outboxDispatcher.dispatchDue()
}
