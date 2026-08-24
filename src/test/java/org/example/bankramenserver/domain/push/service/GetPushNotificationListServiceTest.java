package org.example.bankramenserver.domain.push.service;

import org.example.bankramenserver.domain.push.domain.PushNotification;
import org.example.bankramenserver.domain.push.domain.repository.PushNotificationRepository;
import org.example.bankramenserver.domain.push.presentation.dto.PushNotificationListResponse;
import org.example.bankramenserver.domain.user.domain.User;
import org.example.bankramenserver.domain.user.facade.UserFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPushNotificationListServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PAYMENT_NOTIFICATION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID REPORT_NOTIFICATION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private UserFacade userFacade;

    @Mock
    private PushNotificationRepository pushNotificationRepository;

    private GetPushNotificationListService service;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-05-29T05:30:00Z"), ZoneId.of("Asia/Seoul"));
        service = new GetPushNotificationListService(userFacade, pushNotificationRepository, fixedClock);
    }

    @Test
    void executeReturnsCurrentUsersPushNotificationsForNotificationScreen() {
        PushNotification paymentNotification = pushNotification(
                PAYMENT_NOTIFICATION_ID,
                PushNotification.NotificationType.PAYMENT_RECORDED,
                "결제 내역이 기록됐어요",
                "스타벅스 강남점 4,500원이 카페/간식으로 기록됐어요.",
                "transaction-id",
                LocalDateTime.of(2026, 5, 29, 14, 30)
        );
        PushNotification monthlyReportNotification = pushNotification(
                REPORT_NOTIFICATION_ID,
                PushNotification.NotificationType.MONTHLY_REPORT,
                "월간 리포트가 준비됐어요",
                "2026-05 소비 리포트를 확인해보세요.",
                "2026-05",
                LocalDateTime.of(2026, 5, 28, 9, 0)
        );
        monthlyReportNotification.markAsRead();

        when(userFacade.getCurrentUserId()).thenReturn(USER_ID);
        when(pushNotificationRepository.findAllByUser_IdOrderBySentAtDesc(
                USER_ID,
                PageRequest.of(0, 2)
        )).thenReturn(List.of(paymentNotification, monthlyReportNotification));
        when(pushNotificationRepository.countByUser_IdAndIsReadFalse(USER_ID)).thenReturn(1L);

        PushNotificationListResponse response = service.execute(2);

        assertThat(response.unreadCount()).isEqualTo(1);
        assertThat(response.notifications())
                .extracting(
                        notification -> notification.notificationId(),
                        notification -> notification.type(),
                        notification -> notification.title(),
                        notification -> notification.body(),
                        notification -> notification.referenceKey(),
                        notification -> notification.read(),
                        notification -> notification.unread(),
                        notification -> notification.sentAt(),
                        notification -> notification.displayTime(),
                        notification -> notification.presentation().iconBackgroundColor(),
                        notification -> notification.presentation().unreadIndicatorColor(),
                        notification -> notification.actions().size()
                )
                .containsExactly(
                        tuple(
                                PAYMENT_NOTIFICATION_ID,
                                PushNotification.NotificationType.PAYMENT_RECORDED,
                                "결제 내역이 기록됐어요",
                                "스타벅스 강남점 4,500원이 카페/간식으로 기록됐어요.",
                                "transaction-id",
                                false,
                                true,
                                LocalDateTime.of(2026, 5, 29, 14, 30),
                                "방금 전",
                                "#EAF3FF",
                                "#4F7EEB",
                                0
                        ),
                        tuple(
                                REPORT_NOTIFICATION_ID,
                                PushNotification.NotificationType.MONTHLY_REPORT,
                                "월간 리포트가 준비됐어요",
                                "2026-05 소비 리포트를 확인해보세요.",
                                "2026-05",
                                true,
                                false,
                                LocalDateTime.of(2026, 5, 28, 9, 0),
                                "어제",
                                "#FDF0F2",
                                "#4F7EEB",
                                0
                        )
                );
        verify(pushNotificationRepository).findAllByUser_IdOrderBySentAtDesc(
                USER_ID,
                PageRequest.of(0, 2)
        );
        verify(pushNotificationRepository).countByUser_IdAndIsReadFalse(USER_ID);
    }

    @Test
    void executeReturnsRecurringPatternActions() {
        UUID patternNotificationId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        PushNotification patternNotification = pushNotification(
                patternNotificationId,
                PushNotification.NotificationType.PATTERN_DETECTED,
                "정기결제가 의심돼요!",
                "스포티파이 9,900원이 매월 결제되고 있어요.\n정기결제로 등록하시겠습니까?",
                "recurring-candidate-id",
                LocalDateTime.of(2026, 5, 28, 15, 0)
        );

        when(userFacade.getCurrentUserId()).thenReturn(USER_ID);
        when(pushNotificationRepository.findAllByUser_IdOrderBySentAtDesc(
                USER_ID,
                PageRequest.of(0, 1)
        )).thenReturn(List.of(patternNotification));
        when(pushNotificationRepository.countByUser_IdAndIsReadFalse(USER_ID)).thenReturn(1L);

        PushNotificationListResponse response = service.execute(1);

        assertThat(response.notifications()).hasSize(1);
        assertThat(response.notifications().get(0).presentation().iconBackgroundColor()).isEqualTo("#FFE943");
        assertThat(response.notifications().get(0).displayTime()).isEqualTo("어제");
        assertThat(response.notifications().get(0).actions())
                .extracting(
                        action -> action.code(),
                        action -> action.label(),
                        action -> action.style()
                )
                .containsExactly(
                        tuple("REJECT_RECURRING_PAYMENT", "아니요", "SECONDARY"),
                        tuple("CONFIRM_RECURRING_PAYMENT", "예", "PRIMARY")
                );
    }

    private PushNotification pushNotification(
            UUID id,
            PushNotification.NotificationType type,
            String title,
            String body,
            String referenceKey,
            LocalDateTime sentAt
    ) {
        PushNotification pushNotification = PushNotification.builder()
                .user(User.builder().kakaoId("kakao-1").nickname("사용자").build())
                .type(type)
                .title(title)
                .body(body)
                .referenceKey(referenceKey)
                .build();
        ReflectionTestUtils.setField(pushNotification, "id", id);
        ReflectionTestUtils.setField(pushNotification, "sentAt", sentAt);
        return pushNotification;
    }
}
