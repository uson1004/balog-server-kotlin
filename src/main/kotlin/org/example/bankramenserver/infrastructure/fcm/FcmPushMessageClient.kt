package org.example.bankramenserver.infrastructure.fcm

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.example.bankramenserver.domain.push.service.PushMessageClient
import org.example.bankramenserver.global.error.exception.ErrorCode
import org.example.bankramenserver.global.error.exception.GlobalException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component

@Component
class FcmPushMessageClient(
    private val firebaseMessagingProvider: ObjectProvider<FirebaseMessaging>,
) : PushMessageClient {
    override fun send(token: String, title: String, body: String, data: Map<String, String>) {
        val firebaseMessaging = firebaseMessagingProvider.ifAvailable
        if (firebaseMessaging == null) {
            logger.warn("FCM is disabled or not configured. Skip push notification. token={}", token)
            return
        }
        try {
            firebaseMessaging.send(
                Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .putAllData(data)
                    .build(),
            )
        } catch (exception: Exception) {
            throw GlobalException(ErrorCode.FCM_SEND_FAILED, exception)
        }
    }

    private companion object {
        val logger = LoggerFactory.getLogger(FcmPushMessageClient::class.java)
    }
}
