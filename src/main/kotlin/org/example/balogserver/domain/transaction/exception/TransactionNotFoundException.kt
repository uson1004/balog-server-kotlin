package org.example.balogserver.domain.transaction.exception

import org.example.balogserver.global.error.exception.ErrorCode
import org.example.balogserver.global.error.exception.GlobalException

class TransactionNotFoundException : GlobalException(ErrorCode.TRANSACTION_NOT_FOUND) {
    companion object {
        @JvmField val EXCEPTION: GlobalException = TransactionNotFoundException()
    }
}
