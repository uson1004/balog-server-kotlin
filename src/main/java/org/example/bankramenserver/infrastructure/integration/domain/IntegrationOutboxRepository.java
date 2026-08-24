package org.example.bankramenserver.infrastructure.integration.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface IntegrationOutboxRepository extends JpaRepository<IntegrationOutbox, UUID> {

    List<IntegrationOutbox> findTop50ByStatusInOrderByCreatedAtAsc(List<IntegrationOutbox.Status> statuses);
}
