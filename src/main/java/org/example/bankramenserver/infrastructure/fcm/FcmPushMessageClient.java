package org.example.bankramenserver.infrastructure.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankramenserver.domain.push.service.PushMessageClient;
import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmPushMessageClient implements PushMessageClient {

    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    @Override
    public void send(String token, String title, String body, Map<String, String> data) {
        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null) {
            log.warn("FCM is disabled or not configured. Skip push notification. token={}", token);
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .build();

            firebaseMessaging.send(message);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.FCM_SEND_FAILED, e);
        }
    }
}
