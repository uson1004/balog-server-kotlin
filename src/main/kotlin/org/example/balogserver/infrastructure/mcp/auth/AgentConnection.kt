package org.example.balogserver.infrastructure.mcp.auth

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.util.EnumSet
import java.util.UUID

@Entity
@Table(name = "agent_connections")
class AgentConnection protected constructor() {
    @field:Id
    @field:Column(name = "connection_id", nullable = false, updatable = false)
    final lateinit var connectionId: String
        private set
    @field:Column(name = "consumer_id", nullable = false)
    final lateinit var consumerId: String
        private set
    @field:Column(name = "linked_user_id", nullable = false)
    final var linkedUserId: UUID? = null
        private set
    @field:Column(nullable = false)
    final var enabled = false
        private set
    @field:Column(name = "token_hash", nullable = false, unique = true, length = 64)
    final lateinit var tokenHash: String
        private set
    @field:ElementCollection(fetch = FetchType.EAGER)
    @field:Enumerated(EnumType.STRING)
    @field:CollectionTable(name = "agent_connection_scopes", joinColumns = [JoinColumn(name = "connection_id")])
    @field:Column(name = "scope", nullable = false)
    final var scopes: MutableSet<AgentScope> = EnumSet.noneOf(AgentScope::class.java)
        private set

    private constructor(connectionId: String, consumerId: String, linkedUserId: UUID?, enabled: Boolean, tokenHash: String, scopes: Set<AgentScope>) : this() {
        this.connectionId = connectionId
        this.consumerId = consumerId
        this.linkedUserId = linkedUserId
        this.enabled = enabled
        this.tokenHash = tokenHash
        this.scopes = if (scopes.isEmpty()) EnumSet.noneOf(AgentScope::class.java) else EnumSet.copyOf(scopes)
    }

    fun syncInitialConnection(consumerId: String, linkedUserId: UUID?, enabled: Boolean, tokenHash: String) {
        this.consumerId = consumerId
        this.linkedUserId = linkedUserId
        this.enabled = enabled
        this.tokenHash = tokenHash
        scopes = EnumSet.of(AgentScope.TRANSACTIONS_READ, AgentScope.REPORTS_READ)
    }

    companion object {
        @JvmStatic fun of(connectionId: String, consumerId: String, linkedUserId: UUID?, enabled: Boolean, tokenHash: String, scopes: Set<AgentScope>) = AgentConnection(connectionId, consumerId, linkedUserId, enabled, tokenHash, scopes)
    }
}
