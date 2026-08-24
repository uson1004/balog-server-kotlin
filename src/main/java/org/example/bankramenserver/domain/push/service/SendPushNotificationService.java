package org.example.bankramenserver.domain.push.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankramenserver.domain.push.domain.DeviceToken;
import org.example.bankramenserver.domain.push.domain.PushNotification;
import org.example.bankramenserver.domain.push.domain.repository.DeviceTokenRepository;
import org.example.bankramenserver.domain.push.domain.repository.PushNotificationRepository;
import org.example.bankramenserver.domain.user.domain.User;
import org.example.bankramenserver.domain.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendPushNotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final PushNotificationRepository pushNotificationRepository;
    private final UserRepository userRepository;
    private final PushMessageClient pushMessageClient;

    @Transactional
    public void execute(
            UUID userId,
            PushNotification.NotificationType type,
            String title,
            String body,
            String referenceKey,
            Map<String, String> data
    ) {
        if (referenceKey != null && pushNotificationRepository.existsByUser_IdAndTypeAndReferenceKey(userId, type, referenceKey)) {
            return;
        }

        List<DeviceToken> deviceTokens = deviceTokenRepository.findAllByMemberId(userId);
        deviceTokens.forEach(deviceToken -> sendQuietly(deviceToken.getToken(), title, body, data));
        saveNotificationHistory(userId, type, title, body, referenceKey);
    }

    private void sendQuietly(String token, String title, String body, Map<String, String> data) {
        try {
            pushMessageClient.send(token, title, body, data);
        } catch (RuntimeException e) {
            log.warn("Push notification send failed. token={}, message={}", token, e.getMessage());
        }
    }

    private void saveNotificationHistory(
            UUID userId,
            PushNotification.NotificationType type,
            String title,
            String body,
            String referenceKey
    ) {
        User user = userRepository.getReferenceById(userId);
        PushNotification pushNotification = PushNotification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .referenceKey(referenceKey)
                .build();

        pushNotificationRepository.save(pushNotification);
    }
}
