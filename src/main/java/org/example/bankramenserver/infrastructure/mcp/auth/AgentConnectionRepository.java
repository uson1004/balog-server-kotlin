package org.example.bankramenserver.infrastructure.mcp.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentConnectionRepository extends JpaRepository<AgentConnection, String> {

    Optional<AgentConnection> findByTokenHash(String tokenHash);
}
