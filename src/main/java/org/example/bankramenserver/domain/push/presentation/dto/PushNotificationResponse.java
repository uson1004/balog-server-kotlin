package org.example.bankramenserver.domain.push.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.example.bankramenserver.domain.push.domain.PushNotification;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Schema(description = "푸시 알림 응답")
public record PushNotificationResponse(
        @Schema(description = "푸시 알림 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID notificationId,

        @Schema(description = "푸시 알림 타입", example = "PAYMENT_RECORDED")
        PushNotification.NotificationType type,

        @Schema(description = "푸시 알림 제목", example = "결제 내역이 기록됐어요")
        String title,

        @Schema(description = "푸시 알림 본문", example = "스타벅스 강남점 4,500원이 카페/간식으로 기록됐어요.")
        String body,

        @Schema(description = "참조 키. 거래 ID 또는 리포트 년월 등 알림 타입별 참조값입니다.", example = "550e8400-e29b-41d4-a716-446655440000", nullable = true)
        String referenceKey,

        @Schema(description = "읽음 여부", example = "false")
        boolean read,

        @Schema(description = "읽지 않음 여부. 화면의 파란 점 표시 여부로 사용할 수 있습니다.", example = "true")
        boolean unread,

        @Schema(description = "푸시 알림 발송/저장 시각", example = "2026-05-29T14:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime sentAt,

        @Schema(description = "화면 표시용 상대 시간", example = "2시간 전")
        String displayTime,

        @Schema(description = "알림 타입별 화면 표시 정보")
        Presentation presentation,

        @Schema(description = "알림에서 노출할 액션 버튼 목록")
        List<Action> actions
) {

    private static final String UNREAD_INDICATOR_COLOR = "#4F7EEB";
    private static final DateTimeFormatter SAME_YEAR_DATE_FORMATTER = DateTimeFormatter.ofPattern("M월 d일");
    private static final DateTimeFormatter OTHER_YEAR_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public static PushNotificationResponse from(PushNotification pushNotification, Clock clock) {
        return new PushNotificationResponse(
                pushNotification.getId(),
                pushNotification.getType(),
                pushNotification.getTitle(),
                pushNotification.getBody(),
                pushNotification.getReferenceKey(),
                pushNotification.isRead(),
                !pushNotification.isRead(),
                pushNotification.getSentAt(),
                displayTime(pushNotification.getSentAt(), clock),
                Presentation.from(pushNotification.getType()),
                Action.from(pushNotification.getType())
        );
    }

    private static String displayTime(LocalDateTime sentAt, Clock clock) {
        if (sentAt == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (!sentAt.isBefore(now)) {
            return "방금 전";
        }

        LocalDate sentDate = sentAt.toLocalDate();
        LocalDate today = now.toLocalDate();
        if (sentDate.equals(today)) {
            Duration duration = Duration.between(sentAt, now);
            long minutes = duration.toMinutes();
            if (minutes < 1) {
                return "방금 전";
            }
            if (minutes < 60) {
                return "%d분 전".formatted(minutes);
            }

            return "%d시간 전".formatted(duration.toHours());
        }

        if (sentDate.equals(today.minusDays(1))) {
            return "어제";
        }

        if (sentDate.getYear() == today.getYear()) {
            return sentDate.format(SAME_YEAR_DATE_FORMATTER);
        }

        return sentDate.format(OTHER_YEAR_DATE_FORMATTER);
    }

    @Schema(description = "알림 타입별 화면 표시 정보")
    public record Presentation(
            @Schema(description = "좌측 원형 아이콘 배경색", example = "#EAF3FF")
            String iconBackgroundColor,

            @Schema(description = "읽지 않은 알림 점 색상", example = "#4F7EEB")
            String unreadIndicatorColor
    ) {

        private static Presentation from(PushNotification.NotificationType type) {
            return new Presentation(iconBackgroundColor(type), UNREAD_INDICATOR_COLOR);
        }

        private static String iconBackgroundColor(PushNotification.NotificationType type) {
            return switch (type) {
                case RECURRING_ALERT,
                     PAYMENT_RECORDED,
                     RECURRING_CANDIDATE,
                     RECURRING_PAYMENT_REMINDER,
                     RECURRING_PAYMENT_CONFIRMED,
                     RECURRING_PAYMENT_MISSING -> "#EAF3FF";
                case MONTHLY_REPORT -> "#FDF0F2";
                case PATTERN_DETECTED -> "#FFE943";
            };
        }
    }

    @Schema(description = "알림 액션 버튼")
    public record Action(
            @Schema(description = "액션 코드", example = "CONFIRM_RECURRING_PAYMENT")
            String code,

            @Schema(description = "버튼 라벨", example = "예")
            String label,

            @Schema(description = "버튼 스타일", example = "PRIMARY")
            String style
    ) {

        private static List<Action> from(PushNotification.NotificationType type) {
            if (type != PushNotification.NotificationType.PATTERN_DETECTED) {
                return List.of();
            }

            return List.of(
                    new Action("REJECT_RECURRING_PAYMENT", "아니요", "SECONDARY"),
                    new Action("CONFIRM_RECURRING_PAYMENT", "예", "PRIMARY")
            );
        }
    }
}
