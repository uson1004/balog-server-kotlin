package org.example.bankramenserver.global.error

import org.example.bankramenserver.global.error.exception.ErrorCode

data class ErrorResponse(val status: Int, val code: String, val message: String) {
    companion object { @JvmStatic fun of(errorCode: ErrorCode) = ErrorResponse(errorCode.status.value(), errorCode.name, errorCode.message) }
}
