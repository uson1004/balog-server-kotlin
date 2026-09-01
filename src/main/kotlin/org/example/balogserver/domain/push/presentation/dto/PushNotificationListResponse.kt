package org.example.balogserver.domain.push.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.example.balogserver.domain.push.domain.PushNotification
import java.time.Clock

@Schema(description = "푸시 알림 목록 응답")
data class PushNotificationListResponse(
    @field:Schema(description = "읽지 않은 푸시 알림 개수", example = "1") val unreadCount: Long,
    @field:Schema(description = "푸시 알림 목록") val notifications: List<PushNotificationResponse>,
) {
    companion object {
        @JvmStatic
        fun from(pushNotifications: List<PushNotification>, unreadCount: Long, clock: Clock) =
            PushNotificationListResponse(unreadCount, pushNotifications.map { PushNotificationResponse.from(it, clock) })
    }
}
