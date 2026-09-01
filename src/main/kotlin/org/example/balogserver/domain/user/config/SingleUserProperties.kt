package org.example.balogserver.domain.user.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.UUID

@ConfigurationProperties(prefix = "single-user")
class SingleUserProperties {
    var id: UUID = UUID.fromString("00000000-0000-4000-8000-000000000001")
}
