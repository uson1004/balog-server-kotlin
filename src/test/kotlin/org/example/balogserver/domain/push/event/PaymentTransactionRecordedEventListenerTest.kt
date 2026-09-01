package org.example.balogserver.domain.push.event

import org.example.balogserver.domain.category.domain.Category
import org.example.balogserver.domain.push.domain.PushNotification
import org.example.balogserver.domain.push.service.SendPushNotificationService
import org.example.balogserver.domain.transaction.event.PaymentTransactionRecordedEvent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.Mockito.verify
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class PaymentTransactionRecordedEventListenerTest {
    @Mock lateinit var sendPushNotificationService: SendPushNotificationService

    @Test
    fun handleSendsPaymentRecordedPush() {
        val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val transactionId = UUID.fromString("22222222-2222-2222-2222-222222222222")
        PaymentTransactionRecordedEventListener(sendPushNotificationService).handle(PaymentTransactionRecordedEvent(userId, transactionId, UUID.fromString("33333333-3333-3333-3333-333333333333"), "스타벅스 강남점", 4500L, Category.CAFE_SNACK, LocalDate.of(2026, 7, 11)))
        verify(sendPushNotificationService).execute(userId, PushNotification.NotificationType.PAYMENT_RECORDED, "결제 내역이 기록됐어요", "스타벅스 강남점 4,500원이 카페/간식으로 기록됐어요.", transactionId.toString(), mapOf("type" to PushNotification.NotificationType.PAYMENT_RECORDED.name, "transactionId" to transactionId.toString()))
    }
}
