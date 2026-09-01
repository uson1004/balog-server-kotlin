package org.example.balogserver.infrastructure.fcm

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "fcm")
class FcmProperties {
    var enabled = false
    var credentialsPath: String? = null
}
