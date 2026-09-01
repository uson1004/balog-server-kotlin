package org.example.balogserver.domain.push.presentation.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import org.example.balogserver.domain.push.domain.PushNotification
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Schema(description = "푸시 알림 응답")
data class PushNotificationResponse(
    @field:Schema(description = "푸시 알림 ID", example = "550e8400-e29b-41d4-a716-446655440000") val notificationId: UUID,
    @field:Schema(description = "푸시 알림 타입", example = "PAYMENT_RECORDED") val type: PushNotification.NotificationType,
    @field:Schema(description = "푸시 알림 제목", example = "결제 내역이 기록됐어요") val title: String,
    @field:Schema(description = "푸시 알림 본문", example = "스타벅스 강남점 4,500원이 카페/간식으로 기록됐어요.") val body: String,
    @field:Schema(description = "참조 키. 거래 ID 또는 리포트 년월 등 알림 타입별 참조값입니다.", example = "550e8400-e29b-41d4-a716-446655440000", nullable = true) val referenceKey: String?,
    @field:Schema(description = "읽음 여부", example = "false") val read: Boolean,
    @field:Schema(description = "읽지 않음 여부. 화면의 파란 점 표시 여부로 사용할 수 있습니다.", example = "true") val unread: Boolean,
    @field:Schema(description = "푸시 알림 발송/저장 시각", example = "2026-05-29T14:30:00") @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") val sentAt: LocalDateTime?,
    @field:Schema(description = "화면 표시용 상대 시간", example = "2시간 전") val displayTime: String,
    @field:Schema(description = "알림 타입별 화면 표시 정보") val presentation: Presentation,
    @field:Schema(description = "알림에서 노출할 액션 버튼 목록") val actions: List<Action>,
) {
    companion object {
        private val sameYearDateFormatter = DateTimeFormatter.ofPattern("M월 d일")
        private val otherYearDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

        @JvmStatic
        fun from(pushNotification: PushNotification, clock: Clock) = PushNotificationResponse(
            pushNotification.id!!, pushNotification.type, pushNotification.title, pushNotification.body,
            pushNotification.referenceKey, pushNotification.isRead, !pushNotification.isRead, pushNotification.sentAt,
            displayTime(pushNotification.sentAt, clock), Presentation.from(pushNotification.type), Action.from(pushNotification.type),
        )

        private fun displayTime(sentAt: LocalDateTime?, clock: Clock): String {
            if (sentAt == null) return ""
            val now = LocalDateTime.now(clock)
            if (!sentAt.isBefore(now)) return "방금 전"
            val sentDate = sentAt.toLocalDate()
            val today = now.toLocalDate()
            if (sentDate == today) {
                val duration = Duration.between(sentAt, now)
                return when {
                    duration.toMinutes() < 1 -> "방금 전"
                    duration.toMinutes() < 60 -> "${duration.toMinutes()}분 전"
                    else -> "${duration.toHours()}시간 전"
                }
            }
            if (sentDate == today.minusDays(1)) return "어제"
            return sentDate.format(if (sentDate.year == today.year) sameYearDateFormatter else otherYearDateFormatter)
        }
    }

    @Schema(description = "알림 타입별 화면 표시 정보")
    data class Presentation(
        @field:Schema(description = "좌측 원형 아이콘 배경색", example = "#EAF3FF") val iconBackgroundColor: String,
        @field:Schema(description = "읽지 않은 알림 점 색상", example = "#4F7EEB") val unreadIndicatorColor: String,
    ) {
        companion object {
            fun from(type: PushNotification.NotificationType) = Presentation(
                when (type) {
                    PushNotification.NotificationType.MONTHLY_REPORT -> "#FDF0F2"
                    PushNotification.NotificationType.PATTERN_DETECTED -> "#FFE943"
                    else -> "#EAF3FF"
                }, "#4F7EEB",
            )
        }
    }

    @Schema(description = "알림 액션 버튼")
    data class Action(
        @field:Schema(description = "액션 코드", example = "CONFIRM_RECURRING_PAYMENT") val code: String,
        @field:Schema(description = "버튼 라벨", example = "예") val label: String,
        @field:Schema(description = "버튼 스타일", example = "PRIMARY") val style: String,
    ) {
        companion object {
            fun from(type: PushNotification.NotificationType) = if (type == PushNotification.NotificationType.PATTERN_DETECTED) {
                listOf(Action("REJECT_RECURRING_PAYMENT", "아니요", "SECONDARY"), Action("CONFIRM_RECURRING_PAYMENT", "예", "PRIMARY"))
            } else emptyList()
        }
    }
}
