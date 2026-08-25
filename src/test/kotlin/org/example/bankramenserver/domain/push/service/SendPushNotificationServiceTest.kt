package org.example.bankramenserver.domain.push.service

import org.assertj.core.api.Assertions.assertThat
import org.example.bankramenserver.domain.push.domain.DeviceToken
import org.example.bankramenserver.domain.push.domain.PushNotification
import org.example.bankramenserver.domain.push.domain.repository.DeviceTokenRepository
import org.example.bankramenserver.domain.push.domain.repository.PushNotificationRepository
import org.example.bankramenserver.domain.user.domain.User
import org.example.bankramenserver.domain.user.domain.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class SendPushNotificationServiceTest {
    @Mock lateinit var deviceTokenRepository: DeviceTokenRepository
    @Mock lateinit var pushNotificationRepository: PushNotificationRepository
    @Mock lateinit var userRepository: UserRepository
    @Mock lateinit var pushMessageClient: PushMessageClient
    private lateinit var service: SendPushNotificationService
    private val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    @BeforeEach fun setUp() { service = SendPushNotificationService(deviceTokenRepository, pushNotificationRepository, userRepository, pushMessageClient) }
    @Test fun executeSendsPushToAllDeviceTokensAndSavesHistory() {
        val user = User.builder().nickname("사용자").build()
        `when`(pushNotificationRepository.existsByUser_IdAndTypeAndReferenceKey(userId, PushNotification.NotificationType.PAYMENT_RECORDED, "transaction-id")).thenReturn(false)
        `when`(deviceTokenRepository.findAllByMemberId(userId)).thenReturn(listOf(DeviceToken.builder().memberId(userId).token("token-1").build(), DeviceToken.builder().memberId(userId).token("token-2").build()))
        `when`(userRepository.getReferenceById(userId)).thenReturn(user)
        service.execute(userId, PushNotification.NotificationType.PAYMENT_RECORDED, "결제 내역이 기록됐어요", "본문", "transaction-id", mapOf("type" to "PAYMENT_RECORDED"))
        verify(pushMessageClient).send("token-1", "결제 내역이 기록됐어요", "본문", mapOf("type" to "PAYMENT_RECORDED")); verify(pushMessageClient).send("token-2", "결제 내역이 기록됐어요", "본문", mapOf("type" to "PAYMENT_RECORDED"))
        val captor = ArgumentCaptor.forClass(PushNotification::class.java); verify(pushNotificationRepository).save(captor.capture())
        assertThat(captor.value.user).isEqualTo(user); assertThat(captor.value.type).isEqualTo(PushNotification.NotificationType.PAYMENT_RECORDED); assertThat(captor.value.referenceKey).isEqualTo("transaction-id")
    }
    @Test fun executeSkipsDuplicateReferenceKey() {
        `when`(pushNotificationRepository.existsByUser_IdAndTypeAndReferenceKey(userId, PushNotification.NotificationType.MONTHLY_REPORT, "2026-05")).thenReturn(true)
        service.execute(userId, PushNotification.NotificationType.MONTHLY_REPORT, "월간 리포트가 준비됐어요", "본문", "2026-05", mapOf("type" to "MONTHLY_REPORT"))
        verifyNoInteractions(deviceTokenRepository, userRepository, pushMessageClient)
    }
}
