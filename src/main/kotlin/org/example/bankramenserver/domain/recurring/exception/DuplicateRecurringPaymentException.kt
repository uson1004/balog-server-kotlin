package org.example.bankramenserver.domain.recurring.exception

import org.example.bankramenserver.global.error.exception.ErrorCode
import org.example.bankramenserver.global.error.exception.GlobalException

class DuplicateRecurringPaymentException : GlobalException(ErrorCode.DUPLICATE_RECURRING_PAYMENT)
