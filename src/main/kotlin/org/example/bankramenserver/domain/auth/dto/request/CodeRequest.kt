package org.example.bankramenserver.domain.auth.dto.request
import jakarta.validation.constraints.NotBlank
data class CodeRequest(@field:NotBlank(message = "인가 코드는 필수입니다.") val code: String) { fun code() = code }
