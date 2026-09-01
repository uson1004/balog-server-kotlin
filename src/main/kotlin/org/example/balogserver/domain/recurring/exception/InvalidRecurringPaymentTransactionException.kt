package org.example.balogserver.domain.recurring.exception

import org.example.balogserver.global.error.exception.ErrorCode
import org.example.balogserver.global.error.exception.GlobalException

class InvalidRecurringPaymentTransactionException : GlobalException(ErrorCode.INVALID_RECURRING_PAYMENT_TRANSACTION)
