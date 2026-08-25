package org.example.bankramenserver.global.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "jwt")
class JwtProperties { var secretKey: String? = null; var header: String? = null; var prefix: String? = null; var accessExp: Long = 0; var refreshExp: Long = 0 }
