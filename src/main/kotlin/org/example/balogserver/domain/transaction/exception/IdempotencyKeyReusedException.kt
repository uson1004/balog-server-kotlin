package org.example.balogserver.domain.transaction.exception

import org.example.balogserver.global.error.exception.ErrorCode
import org.example.balogserver.global.error.exception.GlobalException

class IdempotencyKeyReusedException : GlobalException(ErrorCode.IDEMPOTENCY_KEY_REUSED) {
    companion object { val EXCEPTION = IdempotencyKeyReusedException() }
}
