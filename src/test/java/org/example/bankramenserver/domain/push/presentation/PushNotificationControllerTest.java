package org.example.bankramenserver.domain.push.presentation;

import org.example.bankramenserver.domain.push.domain.PushNotification;
import org.example.bankramenserver.domain.push.presentation.dto.PushNotificationListResponse;
import org.example.bankramenserver.domain.push.presentation.dto.PushNotificationResponse;
import org.example.bankramenserver.domain.push.service.GetPushNotificationListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PushNotificationControllerTest {

    private static final UUID NOTIFICATION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private GetPushNotificationListService getPushNotificationListService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PushNotificationController(
                        getPushNotificationListService
                ))
                .build();
    }

    @Test
    void getPushNotificationsReturnsNotifications() throws Exception {
        PushNotificationListResponse response = new PushNotificationListResponse(1L, List.of(
                new PushNotificationResponse(
                        NOTIFICATION_ID,
                        PushNotification.NotificationType.PAYMENT_RECORDED,
                        "결제 내역이 기록됐어요",
                        "스타벅스 강남점 4,500원이 카페/간식으로 기록됐어요.",
                        "transaction-id",
                        false,
                        true,
                        LocalDateTime.of(2026, 5, 29, 14, 30),
                        "2시간 전",
                        new PushNotificationResponse.Presentation("#EAF3FF", "#4F7EEB"),
                        List.of()
                )
        ));
        when(getPushNotificationListService.execute(10)).thenReturn(response);

        mockMvc.perform(get("/push-notifications")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1))
                .andExpect(jsonPath("$.notifications[0].notificationId").value(NOTIFICATION_ID.toString()))
                .andExpect(jsonPath("$.notifications[0].type").value("PAYMENT_RECORDED"))
                .andExpect(jsonPath("$.notifications[0].title").value("결제 내역이 기록됐어요"))
                .andExpect(jsonPath("$.notifications[0].body").value("스타벅스 강남점 4,500원이 카페/간식으로 기록됐어요."))
                .andExpect(jsonPath("$.notifications[0].referenceKey").value("transaction-id"))
                .andExpect(jsonPath("$.notifications[0].read").value(false))
                .andExpect(jsonPath("$.notifications[0].unread").value(true))
                .andExpect(jsonPath("$.notifications[0].sentAt").value("2026-05-29T14:30:00"))
                .andExpect(jsonPath("$.notifications[0].displayTime").value("2시간 전"))
                .andExpect(jsonPath("$.notifications[0].presentation.iconBackgroundColor").value("#EAF3FF"))
                .andExpect(jsonPath("$.notifications[0].presentation.unreadIndicatorColor").value("#4F7EEB"))
                .andExpect(jsonPath("$.notifications[0].actions").isArray());

        verify(getPushNotificationListService).execute(10);
    }

    @Test
    void getPushNotificationsUsesDefaultLimit() throws Exception {
        PushNotificationListResponse response = new PushNotificationListResponse(0L, List.of());
        when(getPushNotificationListService.execute(20)).thenReturn(response);

        mockMvc.perform(get("/push-notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications").isArray());

        verify(getPushNotificationListService).execute(20);
    }
}
