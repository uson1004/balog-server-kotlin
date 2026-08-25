package org.example.bankramenserver.infrastructure.integration

import org.example.bankramenserver.infrastructure.integration.domain.IntegrationConnectionProperties
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutbox

interface IntegrationConnector {
    fun connectorType(): String

    @Throws(Exception::class)
    fun dispatch(outbox: IntegrationOutbox, connection: IntegrationConnectionProperties.Connection)
}
