package org.example.bankramenserver.infrastructure.mcp.auth;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Table(name = "agent_connections")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgentConnection {

    @Id
    @Column(name = "connection_id", nullable = false, updatable = false)
    private String connectionId;

    @Column(name = "consumer_id", nullable = false)
    private String consumerId;

    @Column(name = "linked_user_id", nullable = false)
    private UUID linkedUserId;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "agent_connection_scopes", joinColumns = @JoinColumn(name = "connection_id"))
    @Column(name = "scope", nullable = false)
    private Set<AgentScope> scopes = EnumSet.noneOf(AgentScope.class);

    private AgentConnection(
            String connectionId,
            String consumerId,
            UUID linkedUserId,
            boolean enabled,
            String tokenHash,
            Set<AgentScope> scopes
    ) {
        this.connectionId = connectionId;
        this.consumerId = consumerId;
        this.linkedUserId = linkedUserId;
        this.enabled = enabled;
        this.tokenHash = tokenHash;
        this.scopes = scopes.isEmpty() ? EnumSet.noneOf(AgentScope.class) : EnumSet.copyOf(scopes);
    }

    public static AgentConnection of(
            String connectionId,
            String consumerId,
            UUID linkedUserId,
            boolean enabled,
            String tokenHash,
            Set<AgentScope> scopes
    ) {
        return new AgentConnection(connectionId, consumerId, linkedUserId, enabled, tokenHash, scopes);
    }

    public void syncInitialConnection(
            String consumerId,
            UUID linkedUserId,
            boolean enabled,
            String tokenHash
    ) {
        this.consumerId = consumerId;
        this.linkedUserId = linkedUserId;
        this.enabled = enabled;
        this.tokenHash = tokenHash;
        this.scopes = EnumSet.of(AgentScope.TRANSACTIONS_READ, AgentScope.REPORTS_READ);
    }

}
