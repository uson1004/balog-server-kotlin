package org.example.bankramenserver.infrastructure.integration;

import org.example.bankramenserver.infrastructure.integration.domain.IntegrationConnectionProperties;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutbox;

public interface IntegrationConnector {

    String connectorType();

    void dispatch(IntegrationOutbox outbox, IntegrationConnectionProperties.Connection connection) throws Exception;
}
