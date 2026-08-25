package org.example.bankramenserver.global.error.exception

open class GlobalException(val errorCode: ErrorCode, cause: Throwable? = null) : RuntimeException(errorCode.message, cause)
