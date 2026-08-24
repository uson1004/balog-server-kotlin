package org.example.bankramenserver.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CodeRequest(

        @NotBlank(message = "인가 코드는 필수입니다.")
        String code

) {
}