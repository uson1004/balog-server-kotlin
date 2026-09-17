package org.example.balogserver.domain.push.service

import org.example.balogserver.domain.push.domain.repository.PushNotificationRepository
import org.example.balogserver.domain.push.exception.PushNotificationNotFoundException
import org.example.balogserver.domain.user.facade.UserFacade
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class MarkPushNotificationAsReadService(
    private val userFacade: UserFacade,
    private val pushNotificationRepository: PushNotificationRepository,
) {
    @Transactional
    fun execute(notificationId: UUID) {
        pushNotificationRepository.findByIdAndUser_Id(notificationId, userFacade.currentUserId)
            .orElseThrow { PushNotificationNotFoundException.EXCEPTION }
            .markAsRead()
    }
}
