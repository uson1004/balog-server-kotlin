package org.example.balogserver.infrastructure.integration.domain

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

@Component
@ConfigurationProperties(prefix = "integration")
class IntegrationConnectionProperties {
    var connections: List<Connection> = emptyList()

    fun findByConnectionId(connectionId: String): Connection? = connections.firstOrNull { it.connectionId == connectionId }

    data class Connection(
        val connectionId: String,
        val consumerId: String,
        val connectorType: String,
        val enabled: Boolean,
        val subscribedEvents: List<String>,
        val webhookUrl: String,
        val secret: String,
    ) {
        fun subscribesTo(eventType: String): Boolean =
            enabled && StringUtils.hasText(webhookUrl) && StringUtils.hasText(secret) && eventType in subscribedEvents
    }
}
