package org.example.balogserver.infrastructure.integration

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class IntegrationOutboxScheduler(
    private val outboxDispatcher: IntegrationOutboxDispatcher,
) {
    @Scheduled(fixedDelayString = "\${integration.dispatch-interval-ms:30000}")
    fun dispatchDue() = outboxDispatcher.dispatchDue()
}
