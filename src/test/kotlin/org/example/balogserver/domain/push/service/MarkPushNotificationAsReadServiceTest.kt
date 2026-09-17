package org.example.balogserver.domain.push.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.example.balogserver.domain.push.domain.PushNotification
import org.example.balogserver.domain.push.domain.repository.PushNotificationRepository
import org.example.balogserver.domain.push.exception.PushNotificationNotFoundException
import org.example.balogserver.domain.user.domain.User
import org.example.balogserver.domain.user.facade.UserFacade
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class MarkPushNotificationAsReadServiceTest {
    @Mock lateinit var userFacade: UserFacade
    @Mock lateinit var pushNotificationRepository: PushNotificationRepository
    private lateinit var service: MarkPushNotificationAsReadService
    private val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    private val notificationId = UUID.fromString("22222222-2222-2222-2222-222222222222")

    @BeforeEach fun setUp() { service = MarkPushNotificationAsReadService(userFacade, pushNotificationRepository) }

    @Test
    fun executeMarksOnlyTheCurrentUsersNotificationAsRead() {
        val notification = PushNotification.builder().user(User.builder().nickname("사용자").build()).type(PushNotification.NotificationType.PAYMENT_RECORDED).title("title").body("body").build()
        ReflectionTestUtils.setField(notification, "id", notificationId)
        `when`(userFacade.currentUserId).thenReturn(userId)
        `when`(pushNotificationRepository.findByIdAndUser_Id(notificationId, userId)).thenReturn(Optional.of(notification))

        service.execute(notificationId)

        assertThat(notification.isRead).isTrue()
        verify(pushNotificationRepository).findByIdAndUser_Id(notificationId, userId)
    }

    @Test
    fun executeRejectsANotificationOutsideTheCurrentUsersScope() {
        `when`(userFacade.currentUserId).thenReturn(userId)
        `when`(pushNotificationRepository.findByIdAndUser_Id(notificationId, userId)).thenReturn(Optional.empty())

        assertThatThrownBy { service.execute(notificationId) }.isInstanceOf(PushNotificationNotFoundException::class.java)
    }
}
