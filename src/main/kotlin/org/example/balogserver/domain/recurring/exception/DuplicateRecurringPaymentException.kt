package org.example.balogserver.domain.recurring.exception

import org.example.balogserver.global.error.exception.ErrorCode
import org.example.balogserver.global.error.exception.GlobalException

class DuplicateRecurringPaymentException : GlobalException(ErrorCode.DUPLICATE_RECURRING_PAYMENT)
