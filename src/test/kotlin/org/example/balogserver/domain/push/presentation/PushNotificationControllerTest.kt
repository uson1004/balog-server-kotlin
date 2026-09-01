package org.example.balogserver.domain.push.presentation

import org.example.balogserver.domain.push.domain.PushNotification
import org.example.balogserver.domain.push.presentation.dto.PushNotificationListResponse
import org.example.balogserver.domain.push.presentation.dto.PushNotificationResponse
import org.example.balogserver.domain.push.service.GetPushNotificationListService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class PushNotificationControllerTest {
    @Mock lateinit var getPushNotificationListService: GetPushNotificationListService
    private lateinit var mockMvc: MockMvc
    @BeforeEach fun setUp() { mockMvc = MockMvcBuilders.standaloneSetup(PushNotificationController(getPushNotificationListService)).build() }

    @Test
    fun getPushNotificationsReturnsNotifications() {
        val id = UUID.fromString("22222222-2222-2222-2222-222222222222")
        `when`(getPushNotificationListService.execute(10)).thenReturn(PushNotificationListResponse(1, listOf(PushNotificationResponse(id, PushNotification.NotificationType.PAYMENT_RECORDED, "결제 내역이 기록됐어요", "스타벅스 강남점 4,500원이 카페/간식으로 기록됐어요.", "transaction-id", false, true, LocalDateTime.of(2026, 5, 29, 14, 30), "2시간 전", PushNotificationResponse.Presentation("#EAF3FF", "#4F7EEB"), emptyList()))))
        mockMvc.perform(get("/push-notifications").param("limit", "10")).andExpect(status().isOk).andExpect(jsonPath("$.unreadCount").value(1)).andExpect(jsonPath("$.notifications[0].notificationId").value(id.toString())).andExpect(jsonPath("$.notifications[0].read").value(false)).andExpect(jsonPath("$.notifications[0].unread").value(true)).andExpect(jsonPath("$.notifications[0].sentAt").value("2026-05-29T14:30:00"))
        verify(getPushNotificationListService).execute(10)
    }

    @Test
    fun getPushNotificationsUsesDefaultLimit() {
        `when`(getPushNotificationListService.execute(20)).thenReturn(PushNotificationListResponse(0, emptyList()))
        mockMvc.perform(get("/push-notifications")).andExpect(status().isOk).andExpect(jsonPath("$.notifications").isArray)
        verify(getPushNotificationListService).execute(20)
    }
}
