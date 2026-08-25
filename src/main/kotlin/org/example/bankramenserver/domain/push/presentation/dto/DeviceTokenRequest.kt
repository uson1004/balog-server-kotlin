package org.example.bankramenserver.domain.push.presentation.dto

import jakarta.validation.constraints.NotBlank

data class DeviceTokenRequest(@field:NotBlank val token: String)
