package org.example.balogserver.domain.recurring.exception

import org.example.balogserver.global.error.exception.ErrorCode
import org.example.balogserver.global.error.exception.GlobalException

class RecurringPaymentAccessDeniedException : GlobalException(ErrorCode.RECURRING_PAYMENT_ACCESS_DENIED)
