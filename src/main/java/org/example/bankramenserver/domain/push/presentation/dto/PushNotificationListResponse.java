package org.example.bankramenserver.domain.push.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.bankramenserver.domain.push.domain.PushNotification;

import java.time.Clock;
import java.util.List;

@Schema(description = "푸시 알림 목록 응답")
public record PushNotificationListResponse(
        @Schema(description = "읽지 않은 푸시 알림 개수", example = "1")
        long unreadCount,

        @Schema(description = "푸시 알림 목록")
        List<PushNotificationResponse> notifications
) {

    public static PushNotificationListResponse from(
            List<PushNotification> pushNotifications,
            long unreadCount,
            Clock clock
    ) {
        return new PushNotificationListResponse(
                unreadCount,
                pushNotifications.stream()
                        .map(pushNotification -> PushNotificationResponse.from(pushNotification, clock))
                        .toList()
        );
    }
}
