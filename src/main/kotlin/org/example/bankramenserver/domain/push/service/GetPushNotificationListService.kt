package org.example.bankramenserver.domain.push.service

import org.example.bankramenserver.domain.push.domain.repository.PushNotificationRepository
import org.example.bankramenserver.domain.push.presentation.dto.PushNotificationListResponse
import org.example.bankramenserver.domain.user.facade.UserFacade
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class GetPushNotificationListService(
    private val userFacade: UserFacade,
    private val pushNotificationRepository: PushNotificationRepository,
    private val clock: Clock,
) {
    @Transactional(readOnly = true)
    fun execute(limit: Int): PushNotificationListResponse {
        val userId = userFacade.currentUserId
        return PushNotificationListResponse.from(
            pushNotificationRepository.findAllByUser_IdOrderBySentAtDesc(userId, PageRequest.of(0, limit)),
            pushNotificationRepository.countByUser_IdAndIsReadFalse(userId),
            clock,
        )
    }
}
