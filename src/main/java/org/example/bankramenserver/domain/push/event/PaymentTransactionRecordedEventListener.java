package org.example.bankramenserver.domain.push.event;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.push.domain.PushNotification;
import org.example.bankramenserver.domain.push.service.SendPushNotificationService;
import org.example.bankramenserver.domain.transaction.event.PaymentTransactionRecordedEvent;
import org.example.bankramenserver.infrastructure.fcm.FcmAsyncConfig;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class PaymentTransactionRecordedEventListener {

    private static final String TITLE = "결제 내역이 기록됐어요";

    private final SendPushNotificationService sendPushNotificationService;

    @Async(FcmAsyncConfig.FCM_PUSH_TASK_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentTransactionRecordedEvent event) {
        sendPushNotificationService.execute(
                event.userId(),
                PushNotification.NotificationType.PAYMENT_RECORDED,
                TITLE,
                buildBody(event),
                event.transactionId().toString(),
                Map.of(
                        "type", PushNotification.NotificationType.PAYMENT_RECORDED.name(),
                        "transactionId", event.transactionId().toString()
                )
        );
    }

    private String buildBody(PaymentTransactionRecordedEvent event) {
        String formattedAmount = NumberFormat.getNumberInstance(Locale.KOREA).format(event.amount());
        return "%s %s원이 %s으로 기록됐어요."
                .formatted(event.title(), formattedAmount, event.category().getDisplayName());
    }
}
