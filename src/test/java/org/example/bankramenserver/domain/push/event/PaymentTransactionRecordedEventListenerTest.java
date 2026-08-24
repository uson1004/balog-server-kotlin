package org.example.bankramenserver.domain.push.event;

import org.example.bankramenserver.domain.category.domain.Category;
import org.example.bankramenserver.domain.push.domain.PushNotification;
import org.example.bankramenserver.domain.push.service.SendPushNotificationService;
import org.example.bankramenserver.domain.transaction.event.PaymentTransactionRecordedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentTransactionRecordedEventListenerTest {

    @Mock
    private SendPushNotificationService sendPushNotificationService;

    @Test
    void handleSendsPaymentRecordedPush() {
        PaymentTransactionRecordedEventListener listener = new PaymentTransactionRecordedEventListener(
                sendPushNotificationService
        );
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID transactionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID eventId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        listener.handle(new PaymentTransactionRecordedEvent(
                userId,
                transactionId,
                eventId,
                "스타벅스 강남점",
                4500L,
                Category.CAFE_SNACK,
                LocalDate.of(2026, 7, 11)
        ));

        verify(sendPushNotificationService).execute(
                userId,
                PushNotification.NotificationType.PAYMENT_RECORDED,
                "결제 내역이 기록됐어요",
                "스타벅스 강남점 4,500원이 카페/간식으로 기록됐어요.",
                transactionId.toString(),
                Map.of(
                        "type", PushNotification.NotificationType.PAYMENT_RECORDED.name(),
                        "transactionId", transactionId.toString()
                )
        );
    }
}
