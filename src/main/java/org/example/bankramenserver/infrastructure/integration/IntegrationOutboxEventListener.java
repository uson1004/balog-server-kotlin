package org.example.bankramenserver.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.transaction.event.PaymentTransactionRecordedEvent;
import org.example.bankramenserver.infrastructure.fcm.FcmAsyncConfig;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class IntegrationOutboxEventListener {

    private final IntegrationOutboxWriter outboxWriter;
    private final IntegrationOutboxDispatcher outboxDispatcher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void record(PaymentTransactionRecordedEvent event) {
        outboxWriter.record(event);
    }

    @Async(FcmAsyncConfig.FCM_PUSH_TASK_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void dispatch(PaymentTransactionRecordedEvent event) {
        outboxDispatcher.dispatchDue();
    }
}
