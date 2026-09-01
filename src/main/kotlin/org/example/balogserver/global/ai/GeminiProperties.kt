package org.example.balogserver.global.ai

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "gemini")
class GeminiProperties { var apiKey: String? = null; var model: String = "gemini-2.5-flash"; var baseUrl: String = "https://generativelanguage.googleapis.com/v1beta" }
