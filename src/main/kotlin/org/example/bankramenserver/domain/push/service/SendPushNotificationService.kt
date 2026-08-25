package org.example.bankramenserver.domain.push.service

import org.example.bankramenserver.domain.push.domain.DeviceToken
import org.example.bankramenserver.domain.push.domain.PushNotification
import org.example.bankramenserver.domain.push.domain.repository.DeviceTokenRepository
import org.example.bankramenserver.domain.push.domain.repository.PushNotificationRepository
import org.example.bankramenserver.domain.user.domain.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class SendPushNotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val pushNotificationRepository: PushNotificationRepository,
    private val userRepository: UserRepository,
    private val pushMessageClient: PushMessageClient,
) {
    @Transactional
    fun execute(userId: UUID, type: PushNotification.NotificationType, title: String, body: String, referenceKey: String?, data: Map<String, String>) {
        if (referenceKey != null && pushNotificationRepository.existsByUser_IdAndTypeAndReferenceKey(userId, type, referenceKey)) return
        deviceTokenRepository.findAllByMemberId(userId).forEach { sendQuietly(it.token, title, body, data) }
        val notification = PushNotification.builder().user(userRepository.getReferenceById(userId)).type(type).title(title).body(body).referenceKey(referenceKey).build()
        pushNotificationRepository.save(notification)
    }

    private fun sendQuietly(token: String, title: String, body: String, data: Map<String, String>) {
        try { pushMessageClient.send(token, title, body, data) }
        catch (error: RuntimeException) { log.warn("Push notification send failed. token={}, message={}", token, error.message) }
    }

    private companion object { val log = LoggerFactory.getLogger(SendPushNotificationService::class.java) }
}
