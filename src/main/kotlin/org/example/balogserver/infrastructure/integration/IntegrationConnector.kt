package org.example.balogserver.infrastructure.integration

import org.example.balogserver.infrastructure.integration.domain.IntegrationConnectionProperties
import org.example.balogserver.infrastructure.integration.domain.IntegrationOutbox

interface IntegrationConnector {
    fun connectorType(): String

    @Throws(Exception::class)
    fun dispatch(outbox: IntegrationOutbox, connection: IntegrationConnectionProperties.Connection)
}
