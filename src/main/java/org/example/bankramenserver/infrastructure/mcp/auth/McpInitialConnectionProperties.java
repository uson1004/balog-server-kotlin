package org.example.bankramenserver.infrastructure.mcp.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "mcp.initial-connection")
public class McpInitialConnectionProperties {

    private String connectionId;
    private String consumerId;
    private String linkedUserId;
    private boolean enabled = true;
    private String token;
}
