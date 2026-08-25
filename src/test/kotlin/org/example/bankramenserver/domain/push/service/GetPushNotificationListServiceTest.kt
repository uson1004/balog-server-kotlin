package org.example.bankramenserver.domain.push.service

import org.assertj.core.api.Assertions.assertThat
import org.example.bankramenserver.domain.push.domain.PushNotification
import org.example.bankramenserver.domain.push.domain.repository.PushNotificationRepository
import org.example.bankramenserver.domain.user.domain.User
import org.example.bankramenserver.domain.user.facade.UserFacade
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageRequest
import org.springframework.test.util.ReflectionTestUtils
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class GetPushNotificationListServiceTest {
    @Mock lateinit var userFacade: UserFacade
    @Mock lateinit var pushNotificationRepository: PushNotificationRepository
    private lateinit var service: GetPushNotificationListService
    private val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    @BeforeEach fun setUp() { service = GetPushNotificationListService(userFacade, pushNotificationRepository, Clock.fixed(Instant.parse("2026-05-29T05:30:00Z"), ZoneId.of("Asia/Seoul"))) }

    @Test
    fun executeReturnsCurrentUsersPushNotificationsForNotificationScreen() {
        val payment = notification(UUID.fromString("22222222-2222-2222-2222-222222222222"), PushNotification.NotificationType.PAYMENT_RECORDED, LocalDateTime.of(2026, 5, 29, 14, 30))
        val report = notification(UUID.fromString("33333333-3333-3333-3333-333333333333"), PushNotification.NotificationType.MONTHLY_REPORT, LocalDateTime.of(2026, 5, 28, 9, 0)); report.markAsRead()
        `when`(userFacade.currentUserId).thenReturn(userId)
        `when`(pushNotificationRepository.findAllByUser_IdOrderBySentAtDesc(userId, PageRequest.of(0, 2))).thenReturn(listOf(payment, report))
        `when`(pushNotificationRepository.countByUser_IdAndIsReadFalse(userId)).thenReturn(1)
        val response = service.execute(2)
        assertThat(response.unreadCount).isEqualTo(1)
        assertThat(response.notifications.map { listOf(it.type, it.read, it.unread, it.displayTime, it.presentation.iconBackgroundColor) }).containsExactly(listOf(PushNotification.NotificationType.PAYMENT_RECORDED, false, true, "방금 전", "#EAF3FF"), listOf(PushNotification.NotificationType.MONTHLY_REPORT, true, false, "어제", "#FDF0F2"))
        verify(pushNotificationRepository).findAllByUser_IdOrderBySentAtDesc(userId, PageRequest.of(0, 2))
    }

    @Test
    fun executeReturnsRecurringPatternActions() {
        val pattern = notification(UUID.fromString("44444444-4444-4444-4444-444444444444"), PushNotification.NotificationType.PATTERN_DETECTED, LocalDateTime.of(2026, 5, 28, 15, 0))
        `when`(userFacade.currentUserId).thenReturn(userId)
        `when`(pushNotificationRepository.findAllByUser_IdOrderBySentAtDesc(userId, PageRequest.of(0, 1))).thenReturn(listOf(pattern))
        `when`(pushNotificationRepository.countByUser_IdAndIsReadFalse(userId)).thenReturn(1)
        assertThat(service.execute(1).notifications.single().actions.map { it.code }).containsExactly("REJECT_RECURRING_PAYMENT", "CONFIRM_RECURRING_PAYMENT")
    }

    private fun notification(id: UUID, type: PushNotification.NotificationType, sentAt: LocalDateTime): PushNotification {
        val notification = PushNotification.builder().user(User.builder().kakaoId("kakao-1").nickname("사용자").build()).type(type).title("title").body("body").referenceKey("key").build()
        ReflectionTestUtils.setField(notification, "id", id); ReflectionTestUtils.setField(notification, "sentAt", sentAt)
        return notification
    }
}
