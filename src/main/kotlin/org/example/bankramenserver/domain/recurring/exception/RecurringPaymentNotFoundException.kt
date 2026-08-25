package org.example.bankramenserver.domain.recurring.exception

import org.example.bankramenserver.global.error.exception.ErrorCode
import org.example.bankramenserver.global.error.exception.GlobalException

class RecurringPaymentNotFoundException : GlobalException(ErrorCode.RECURRING_PAYMENT_NOT_FOUND)
