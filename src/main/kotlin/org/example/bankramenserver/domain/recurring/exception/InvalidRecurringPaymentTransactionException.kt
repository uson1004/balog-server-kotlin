package org.example.bankramenserver.domain.recurring.exception

import org.example.bankramenserver.global.error.exception.ErrorCode
import org.example.bankramenserver.global.error.exception.GlobalException

class InvalidRecurringPaymentTransactionException : GlobalException(ErrorCode.INVALID_RECURRING_PAYMENT_TRANSACTION)
