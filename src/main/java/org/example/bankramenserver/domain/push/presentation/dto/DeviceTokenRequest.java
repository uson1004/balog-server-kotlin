package org.example.bankramenserver.domain.push.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record DeviceTokenRequest(

        @NotBlank
        String token
) {
}