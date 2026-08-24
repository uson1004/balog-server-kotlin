package org.example.bankramenserver.domain.push.service;

import org.example.bankramenserver.domain.push.domain.DeviceToken;
import org.example.bankramenserver.domain.push.domain.PushNotification;
import org.example.bankramenserver.domain.push.domain.repository.DeviceTokenRepository;
import org.example.bankramenserver.domain.push.domain.repository.PushNotificationRepository;
import org.example.bankramenserver.domain.user.domain.User;
import org.example.bankramenserver.domain.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendPushNotificationServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private PushNotificationRepository pushNotificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PushMessageClient pushMessageClient;

    private SendPushNotificationService service;

    @BeforeEach
    void setUp() {
        service = new SendPushNotificationService(
                deviceTokenRepository,
                pushNotificationRepository,
                userRepository,
                pushMessageClient
        );
    }

    @Test
    void executeSendsPushToAllDeviceTokensAndSavesHistory() {
        User user = User.builder().nickname("사용자").build();
        when(pushNotificationRepository.existsByUser_IdAndTypeAndReferenceKey(
                USER_ID,
                PushNotification.NotificationType.PAYMENT_RECORDED,
                "transaction-id"
        )).thenReturn(false);
        when(deviceTokenRepository.findAllByMemberId(USER_ID)).thenReturn(List.of(
                DeviceToken.builder().memberId(USER_ID).token("token-1").build(),
                DeviceToken.builder().memberId(USER_ID).token("token-2").build()
        ));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);

        service.execute(
                USER_ID,
                PushNotification.NotificationType.PAYMENT_RECORDED,
                "결제 내역이 기록됐어요",
                "스타벅스 강남점 4,500원이 카페/간식으로 기록됐어요.",
                "transaction-id",
                Map.of("type", "PAYMENT_RECORDED")
        );

        verify(pushMessageClient).send(
                "token-1",
                "결제 내역이 기록됐어요",
                "스타벅스 강남점 4,500원이 카페/간식으로 기록됐어요.",
                Map.of("type", "PAYMENT_RECORDED")
        );
        verify(pushMessageClient).send(
                "token-2",
                "결제 내역이 기록됐어요",
                "스타벅스 강남점 4,500원이 카페/간식으로 기록됐어요.",
                Map.of("type", "PAYMENT_RECORDED")
        );

        ArgumentCaptor<PushNotification> captor = ArgumentCaptor.forClass(PushNotification.class);
        verify(pushNotificationRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getType()).isEqualTo(PushNotification.NotificationType.PAYMENT_RECORDED);
        assertThat(captor.getValue().getReferenceKey()).isEqualTo("transaction-id");
    }

    @Test
    void executeSkipsDuplicateReferenceKey() {
        when(pushNotificationRepository.existsByUser_IdAndTypeAndReferenceKey(
                USER_ID,
                PushNotification.NotificationType.MONTHLY_REPORT,
                "2026-05"
        )).thenReturn(true);

        service.execute(
                USER_ID,
                PushNotification.NotificationType.MONTHLY_REPORT,
                "월간 리포트가 준비됐어요",
                "2026-05 소비 리포트를 확인해보세요.",
                "2026-05",
                Map.of("type", "MONTHLY_REPORT")
        );

        verifyNoInteractions(deviceTokenRepository, userRepository, pushMessageClient);
    }
}
