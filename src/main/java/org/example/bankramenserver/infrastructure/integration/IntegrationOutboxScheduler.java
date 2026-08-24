package org.example.bankramenserver.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntegrationOutboxScheduler {

    private final IntegrationOutboxDispatcher outboxDispatcher;

    @Scheduled(fixedDelayString = "${integration.dispatch-interval-ms:30000}")
    public void dispatchDue() {
        outboxDispatcher.dispatchDue();
    }
}
