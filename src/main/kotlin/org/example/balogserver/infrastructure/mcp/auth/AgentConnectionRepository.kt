package org.example.balogserver.infrastructure.mcp.auth

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface AgentConnectionRepository : JpaRepository<AgentConnection, String> {
    fun findByTokenHash(tokenHash: String?): Optional<AgentConnection>
}
