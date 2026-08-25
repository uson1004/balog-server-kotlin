package org.example.bankramenserver.domain.transaction.exception

import org.example.bankramenserver.global.error.exception.ErrorCode
import org.example.bankramenserver.global.error.exception.GlobalException

class TransactionNotFoundException : GlobalException(ErrorCode.TRANSACTION_NOT_FOUND) {
    companion object {
        @JvmField val EXCEPTION: GlobalException = TransactionNotFoundException()
    }
}
