package org.example.balogserver.infrastructure.mcp.auth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mcp.initial-connection")
class McpInitialConnectionProperties {
    var connectionId: String? = null
    var consumerId: String? = null
    var linkedUserId: String? = null
    var enabled = true
    var token: String? = null
}
