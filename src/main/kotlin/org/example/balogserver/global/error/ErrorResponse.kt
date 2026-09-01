package org.example.balogserver.global.error

import org.example.balogserver.global.error.exception.ErrorCode

data class ErrorResponse(val status: Int, val code: String, val message: String) {
    companion object { @JvmStatic fun of(errorCode: ErrorCode) = ErrorResponse(errorCode.status.value(), errorCode.name, errorCode.message) }
}
